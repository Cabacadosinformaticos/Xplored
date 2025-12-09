package com.Xplored.Xplored.Controller;

import com.Xplored.Xplored.Model.Place.PlaceRequest;
import com.Xplored.Xplored.Model.Place.PlaceResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.web.bind.annotation.*;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/places")
@CrossOrigin(origins = "*")
public class PlaceController {

    // Moderation: places by USER start pending; places by ADMIN are approved at once.
    private static final String STATUS_APPROVED = "APPROVED";
    private static final String STATUS_PENDING = "PENDING";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final RowMapper<PlaceResponse> PLACE_MAPPER = (rs, rowNum) -> new PlaceResponse(
            rs.getLong("place_id"),
            rs.getString("name"),
            rs.getString("description"),
            rs.getDouble("lat"),
            rs.getDouble("lng"),
            rs.getString("address_full"),
            rs.getLong("category_id"),
            rs.getString("author_email"), // Returns email as authorId for frontend logic
            rs.getDouble("avg_rating"),
            rs.getString("cover_image_url"),
            rs.getBoolean("is_verified"),
            rs.getString("status")
    );

    private static final String SELECT_PLACES = """
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
        """;

    // --- 1. Get All Places ---
    // Approved places for everyone; with userEmail the caller also sees their own pending ones.
    @GetMapping("")
    public List<PlaceResponse> getAllPlaces(@RequestParam(required = false) String userEmail) {
        if (userEmail == null || userEmail.isBlank()) {
            return jdbcTemplate.query(SELECT_PLACES + " WHERE p.status = '" + STATUS_APPROVED + "'",
                    PLACE_MAPPER);
        }
        String sql = SELECT_PLACES
                + " WHERE p.status = '" + STATUS_APPROVED + "'"
                + " OR (u.email = ? AND p.status = '" + STATUS_PENDING + "')";
        return jdbcTemplate.query(sql, PLACE_MAPPER, userEmail);
    }

    // --- 2. Create Place ---
    @PostMapping("")
    public ResponseEntity<?> createPlace(@RequestBody PlaceRequest req) {
        if (req.authorId == null || req.authorId.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Author is required"));
        }
        // Resolve author email to database id.
        List<Map<String, Object>> authors = jdbcTemplate.query(
                "SELECT id, role FROM users WHERE email = ?",
                (rs, rowNum) -> Map.of("id", rs.getLong("id"), "role", rs.getString("role")),
                req.authorId);
        if (authors.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Unknown author"));
        }
        Long userId = (Long) authors.get(0).get("id");
        boolean isAdmin = "ADMIN".equalsIgnoreCase((String) authors.get(0).get("role"));

        if (req.categoryId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Category is required"));
        }
        Integer categoryCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM categories WHERE id = ?", Integer.class, req.categoryId);
        if (categoryCount == null || categoryCount == 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "Unknown category"));
        }

        // Moderation rule: an ADMIN publishes straight away, a USER waits for approval.
        String status = isAdmin ? STATUS_APPROVED : STATUS_PENDING;
        boolean verified = isAdmin;

        String sql = """
            INSERT INTO places (name, description, lat, lng, address_full, category_id, author_id, avg_rating, is_verified, status)
            VALUES (?, ?, ?, ?, ?, ?, ?, 0.0, ?, ?)
        """;

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, req.name);
            ps.setString(2, req.description);
            ps.setDouble(3, req.lat);
            ps.setDouble(4, req.lng);
            ps.setString(5, req.addressFull);
            ps.setLong(6, req.categoryId);
            ps.setLong(7, userId);
            ps.setBoolean(8, verified);
            ps.setString(9, status);
            return ps;
        }, keyHolder);

        long newId = keyHolder.getKey().longValue();

        return ResponseEntity.status(HttpStatus.CREATED).body(new PlaceResponse(
                newId, req.name, req.description, req.lat, req.lng,
                req.addressFull, req.categoryId, req.authorId, 0.0, null, verified, status
        ));
    }

    // --- 3. Delete Place (author only) ---
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePlace(@PathVariable Long id, @RequestParam String userEmail) {
        List<String> authors = jdbcTemplate.query(
                "SELECT u.email FROM places p LEFT JOIN users u ON p.author_id = u.id WHERE p.id = ?",
                (rs, rowNum) -> rs.getString("email"),
                id);
        if (authors.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Place not found"));
        }
        String author = authors.get(0);
        if (author == null || !author.equalsIgnoreCase(userEmail)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Only the author can delete this place"));
        }
        jdbcTemplate.update("DELETE FROM places WHERE id = ?", id);
        return ResponseEntity.ok(Map.of("message", "Deleted"));
    }
}
