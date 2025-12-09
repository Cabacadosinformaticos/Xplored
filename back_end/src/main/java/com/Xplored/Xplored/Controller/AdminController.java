package com.Xplored.Xplored.Controller;

import com.Xplored.Xplored.Model.Place.PlaceResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// Moderation of new places: only an ADMIN can list, approve or reject them.
@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    private static final String STATUS_APPROVED = "APPROVED";
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_REJECTED = "REJECTED";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // --- 1. Places waiting for approval ---
    @GetMapping("/places/pending")
    public ResponseEntity<?> getPendingPlaces(@RequestParam String adminEmail) {
        if (!isAdmin(adminEmail)) {
            return forbidden();
        }
        String sql = """
            SELECT
                p.id AS place_id,
                p.name,
                p.description,
                p.lat,
                p.lng,
                p.address_full,
                p.category_id,
                p.avg_rating,
                p.is_verified,
                p.status,
                u.email AS author_email,
                (SELECT ph.url FROM photos ph
                 WHERE ph.place_id = p.id AND ph.kind = 'GALLERY'
                 ORDER BY ph.id ASC LIMIT 1) AS cover_image_url
            FROM places p
            LEFT JOIN users u ON p.author_id = u.id
            WHERE p.status = ?
            ORDER BY p.created_at ASC
        """;
        List<PlaceResponse> pending = jdbcTemplate.query(sql, (rs, rowNum) -> new PlaceResponse(
                rs.getLong("place_id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getDouble("lat"),
                rs.getDouble("lng"),
                rs.getString("address_full"),
                rs.getLong("category_id"),
                rs.getString("author_email"),
                rs.getDouble("avg_rating"),
                rs.getString("cover_image_url"),
                rs.getBoolean("is_verified"),
                rs.getString("status")
        ), STATUS_PENDING);
        return ResponseEntity.ok(pending);
    }

    // --- 2. Approve a pending place ---
    @PostMapping("/places/{id}/approve")
    public ResponseEntity<?> approvePlace(@PathVariable Long id, @RequestParam String adminEmail) {
        return review(id, adminEmail, STATUS_APPROVED, true);
    }

    // --- 3. Reject a pending place ---
    @PostMapping("/places/{id}/reject")
    public ResponseEntity<?> rejectPlace(@PathVariable Long id, @RequestParam String adminEmail) {
        return review(id, adminEmail, STATUS_REJECTED, false);
    }

    private ResponseEntity<?> review(Long placeId, String adminEmail, String status, boolean verified) {
        if (!isAdmin(adminEmail)) {
            return forbidden();
        }
        if (count("SELECT COUNT(*) FROM places WHERE id = ?", placeId) == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Place not found"));
        }
        jdbcTemplate.update("UPDATE places SET status = ?, is_verified = ? WHERE id = ?",
                status, verified, placeId);
        return ResponseEntity.ok(Map.of("message", "Updated", "status", status));
    }

    private boolean isAdmin(String email) {
        return email != null && !email.isBlank()
                && count("SELECT COUNT(*) FROM users WHERE email = ? AND role = 'ADMIN'", email) > 0;
    }

    private int count(String sql, Object... args) {
        Integer value = jdbcTemplate.queryForObject(sql, Integer.class, args);
        return value == null ? 0 : value;
    }

    private static ResponseEntity<Map<String, String>> forbidden() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "Acesso restrito a administradores"));
    }
}
