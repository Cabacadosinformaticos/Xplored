package com.Xplored.Xplored.Controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/user/password-reset")
@CrossOrigin(origins = "*")
public class PasswordResetController {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetController.class);

    private static final int CODE_TTL_MINUTES = 15;
    private static final int MAX_ATTEMPTS = 5;
    private static final int MIN_PASSWORD_LENGTH = 6;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final SecureRandom random = new SecureRandom();

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Step 1: issue a code. Always answers 200 with the same body, existing account or not,
    // so the endpoint cannot be used to enumerate accounts.
    @PostMapping("/request")
    public ResponseEntity<?> requestReset(@RequestBody Map<String, Object> payload) {
        String email = asString(payload.get("email"));
        if (!email.isBlank()) {
            Long userId = findUserId(email);
            if (userId != null) {
                // Invalidate older unused codes before issuing a fresh one.
                jdbcTemplate.update(
                        "UPDATE password_reset_tokens SET used = TRUE WHERE user_id = ? AND used = FALSE",
                        userId);
                String code = String.format("%06d", random.nextInt(1_000_000));
                // Expiry uses the database clock, the same one the confirm query compares against.
                jdbcTemplate.update(
                        "INSERT INTO password_reset_tokens (user_id, code_hash, expires_at, attempts, used) "
                                + "VALUES (?, ?, DATE_ADD(NOW(), INTERVAL ? MINUTE), 0, FALSE)",
                        userId, passwordEncoder.encode(code), CODE_TTL_MINUTES);
                // There is no mail service: the code is only visible in the server log, by design.
                log.info("Password reset code for {}: {}", email, code);
            }
        }
        return ResponseEntity.ok(Map.of("message", "Se a conta existir, foi gerado um código de recuperação."));
    }

    // Step 2: verify the code and store the new password hash.
    @Transactional
    @PostMapping("/confirm")
    public ResponseEntity<?> confirmReset(@RequestBody Map<String, Object> payload) {
        String email = asString(payload.get("email"));
        String code = asString(payload.get("code"));
        String newPassword = asString(payload.get("newPassword"));

        if (email.isBlank() || code.isBlank() || newPassword.isBlank()) {
            return badRequest("Preenche todos os campos.");
        }
        if (newPassword.length() < MIN_PASSWORD_LENGTH) {
            return badRequest("A palavra-passe deve ter pelo menos 6 caracteres.");
        }

        Long userId = findUserId(email);
        if (userId == null) {
            return badRequest("Código inválido ou expirado.");
        }

        // Newest unused, still valid code of this user.
        List<Map<String, Object>> tokens = jdbcTemplate.query(
                "SELECT id, code_hash, attempts FROM password_reset_tokens "
                        + "WHERE user_id = ? AND used = FALSE AND expires_at > NOW() ORDER BY id DESC LIMIT 1",
                (rs, rowNum) -> Map.of(
                        "id", rs.getLong("id"),
                        "codeHash", rs.getString("code_hash"),
                        "attempts", rs.getInt("attempts")),
                userId);
        if (tokens.isEmpty()) {
            return badRequest("Código inválido ou expirado.");
        }
        Map<String, Object> token = tokens.get(0);
        long tokenId = (Long) token.get("id");
        int attempts = (Integer) token.get("attempts");

        if (attempts >= MAX_ATTEMPTS) {
            return badRequest("Demasiadas tentativas. Pede um novo código.");
        }
        if (!passwordEncoder.matches(code, (String) token.get("codeHash"))) {
            jdbcTemplate.update(
                    "UPDATE password_reset_tokens SET attempts = attempts + 1 WHERE id = ?", tokenId);
            return badRequest("Código inválido ou expirado.");
        }

        jdbcTemplate.update("UPDATE users SET password_hash = ? WHERE id = ?",
                passwordEncoder.encode(newPassword), userId);
        jdbcTemplate.update("UPDATE password_reset_tokens SET used = TRUE WHERE id = ?", tokenId);
        return ResponseEntity.ok(Map.of("message", "Palavra-passe redefinida com sucesso."));
    }

    private Long findUserId(String email) {
        List<Long> ids = jdbcTemplate.query(
                "SELECT id FROM users WHERE email = ?", (rs, rowNum) -> rs.getLong("id"), email);
        return ids.isEmpty() ? null : ids.get(0);
    }

    private static String asString(Object value) {
        return value instanceof String s ? s.trim() : "";
    }

    private static ResponseEntity<Map<String, String>> badRequest(String error) {
        return ResponseEntity.badRequest().body(Map.of("error", error));
    }
}
