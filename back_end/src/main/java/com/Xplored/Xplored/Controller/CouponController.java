package com.Xplored.Xplored.Controller;

import com.Xplored.Xplored.Model.Coupon.CouponResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/coupons")
@CrossOrigin(origins = "*")
public class CouponController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // --- Endpoint 1: Active coupons. The code is never exposed here. ---
    @GetMapping("/active")
    public List<CouponResponse> getActiveCoupons() {
        String sql = """
            SELECT
                c.id,
                c.title,
                c.description,
                c.cost_points,
                b.name AS merchant_name
            FROM coupons c
            JOIN business_accounts b ON c.business_account_id = b.id
            WHERE c.active = TRUE
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> new CouponResponse(
                rs.getLong("id"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getString("merchant_name"),
                rs.getInt("cost_points")
        ));
    }

    // --- Endpoint 2: Redeem a coupon ---
    @Transactional
    @PostMapping("/redeem")
    public ResponseEntity<?> redeemCoupon(@RequestBody Map<String, Object> payload) {
        Object userEmailRaw = payload.get("userEmail");
        Object couponIdRaw = payload.get("couponId");
        if (!(userEmailRaw instanceof String userEmail) || userEmail.isBlank()
                || !(couponIdRaw instanceof Number)) {
            return ResponseEntity.badRequest().body(Map.of("error", "userEmail and couponId are required"));
        }
        Long couponId = ((Number) couponIdRaw).longValue();

        // 1. Fetch user id and points.
        List<Map<String, Object>> users = jdbcTemplate.query(
                "SELECT id, points FROM users WHERE email = ?",
                (rs, rowNum) -> Map.of("id", rs.getLong("id"), "points", rs.getInt("points")),
                userEmail);
        if (users.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        }
        Long userId = (Long) users.get(0).get("id");
        int currentPoints = (Integer) users.get(0).get("points");

        // 2. Fetch the coupon; query() so an unknown id becomes a 404, not a 500.
        List<Map<String, Object>> coupons = jdbcTemplate.query(
                "SELECT cost_points, code, active, title FROM coupons WHERE id = ?",
                (rs, rowNum) -> {
                    Map<String, Object> row = new HashMap<>();
                    row.put("cost", rs.getInt("cost_points"));
                    row.put("code", rs.getString("code"));
                    row.put("active", rs.getBoolean("active"));
                    row.put("title", rs.getString("title"));
                    return row;
                },
                couponId);
        if (coupons.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Coupon not found"));
        }
        Map<String, Object> coupon = coupons.get(0);
        int cost = (Integer) coupon.get("cost");
        String code = (String) coupon.get("code");
        boolean active = (Boolean) coupon.get("active");
        String title = (String) coupon.get("title");

        if (!active) {
            return ResponseEntity.badRequest().body(Map.of("error", "Coupon is not active"));
        }
        if (currentPoints < cost) {
            return ResponseEntity.badRequest().body(Map.of("error", "Pontos insuficientes."));
        }

        // 3. Atomic debit: only succeeds while the balance is still enough.
        int updated = jdbcTemplate.update(
                "UPDATE users SET points = points - ? WHERE id = ? AND points >= ?",
                cost, userId, cost);
        if (updated == 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "Pontos insuficientes."));
        }

        // 4. Record the redemption and the ledger entry.
        jdbcTemplate.update("INSERT INTO redemptions (user_id, coupon_id) VALUES (?, ?)", userId, couponId);
        jdbcTemplate.update(
                "INSERT INTO points_ledger (user_id, amount, reason) VALUES (?, ?, ?)",
                userId, -cost, "Cupão resgatado: " + title);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Success");
        response.put("newBalance", currentPoints - cost);
        response.put("code", code);
        return ResponseEntity.ok(response);
    }

    // --- Endpoint 3: Coupons redeemed by a user, newest first ---
    @GetMapping("/redeemed")
    public List<Map<String, Object>> getRedeemedCoupons(@RequestParam String email) {
        String sql = """
            SELECT c.id AS coupon_id, c.title, b.name AS merchant_name, c.code, r.redeemed_at
            FROM redemptions r
            JOIN coupons c ON r.coupon_id = c.id
            JOIN business_accounts b ON c.business_account_id = b.id
            JOIN users u ON r.user_id = u.id
            WHERE u.email = ?
            ORDER BY r.redeemed_at DESC, r.id DESC
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Map<String, Object> row = new HashMap<>();
            row.put("couponId", rs.getLong("coupon_id"));
            row.put("title", rs.getString("title"));
            row.put("merchantName", rs.getString("merchant_name"));
            row.put("code", rs.getString("code"));
            Timestamp redeemedAt = rs.getTimestamp("redeemed_at");
            row.put("redeemedAt", redeemedAt == null ? null : redeemedAt.toString());
            return row;
        }, email);
    }
}
