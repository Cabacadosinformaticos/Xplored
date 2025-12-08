package com.Xplored.Xplored.Controller;

import com.Xplored.Xplored.Model.Place.PlaceResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/favorites")
@CrossOrigin(origins = "*")
public class FavoriteController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // --- 1. Add a place to the user's favourites (idempotent) ---
    @PostMapping("")
    public ResponseEntity<?> addFavorite(@RequestBody Map<String, Object> payload) {
        Object emailRaw = payload.get("userEmail");
        Object placeIdRaw = payload.get("placeId");
        if (!(emailRaw instanceof String userEmail) || userEmail.isBlank()
                || !(placeIdRaw instanceof Number)) {
            return ResponseEntity.badRequest().body(Map.of("error", "userEmail and placeId are required"));
        }
        Long placeId = ((Number) placeIdRaw).longValue();

        Long userId = findUserId(userEmail);
        if (userId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        }
        if (count("SELECT COUNT(*) FROM places WHERE id = ?", placeId) == 0) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Place not found"));
        }

        if (count("SELECT COUNT(*) FROM favorites WHERE user_id = ? AND place_id = ?", userId, placeId) > 0) {
            return ResponseEntity.ok(Map.of("message", "Already a favourite"));
        }

        jdbcTemplate.update("INSERT INTO favorites (user_id, place_id) VALUES (?, ?)", userId, placeId);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "Favourite added"));
    }

    // --- 2. Remove a place from the user's favourites (idempotent) ---
    @DeleteMapping("")
    public ResponseEntity<?> removeFavorite(@RequestParam String userEmail, @RequestParam Long placeId) {
        Long userId = findUserId(userEmail);
        if (userId != null) {
            jdbcTemplate.update("DELETE FROM favorites WHERE user_id = ? AND place_id = ?", userId, placeId);
        }
        return ResponseEntity.noContent().build();
    }

    // --- 3. Favourites of a user, shaped like /places ---
    @GetMapping("")
    public List<PlaceResponse> getFavorites(@RequestParam String userEmail) {
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
                u.email AS author_email,
                (SELECT ph.url FROM photos ph
                 WHERE ph.place_id = p.id AND ph.kind = 'GALLERY'
                 ORDER BY ph.id ASC LIMIT 1) AS cover_image_url
            FROM favorites f
            JOIN places p ON f.place_id = p.id
            JOIN users fu ON f.user_id = fu.id
            LEFT JOIN users u ON p.author_id = u.id
            WHERE fu.email = ?
            ORDER BY f.created_at DESC
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> new PlaceResponse(
                rs.getLong("place_id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getDouble("lat"),
                rs.getDouble("lng"),
                rs.getString("address_full"),
                rs.getLong("category_id"),
                rs.getString("author_email"),
                rs.getDouble("avg_rating"),
                rs.getString("cover_image_url")
        ), userEmail);
    }

    private Long findUserId(String email) {
        List<Long> ids = jdbcTemplate.query(
                "SELECT id FROM users WHERE email = ?",
                (rs, rowNum) -> rs.getLong("id"),
                email);
        return ids.isEmpty() ? null : ids.get(0);
    }

    private int count(String sql, Object... args) {
        Integer value = jdbcTemplate.queryForObject(sql, Integer.class, args);
        return value == null ? 0 : value;
    }
}
