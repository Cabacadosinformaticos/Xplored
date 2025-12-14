package com.Xplored.Xplored.Controller;

import com.Xplored.Xplored.Model.Place.PlaceResponse;
import com.Xplored.Xplored.Model.Place.PlaceRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@RestController
@RequestMapping("/places")
@CrossOrigin(origins = "*")
public class PlaceController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // --- 1. Get All Places ---
    @GetMapping("")
    public List<PlaceResponse> getAllPlaces() {
        // Updated SQL to match Android expectations
        String sql = """
            SELECT 
                p.place_id, 
                p.name, 
                p.description, 
                p.lat, 
                p.lng, 
                p.address_full, 
                p.category_id, -- We need the ID, not the name, for Android logic
                u.email as author_email,
                -- Live calculation of rating
                COALESCE((SELECT AVG(r.rating) FROM reviews r WHERE r.place_id = p.place_id), 0.0) as calculated_rating,
                p.cover_image_url
            FROM places p
            JOIN users u ON p.user_id = u.user_id
            WHERE p.status = 'APPROVED'
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> new PlaceResponse(
                rs.getLong("place_id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getDouble("lat"),
                rs.getDouble("lng"),
                rs.getString("address_full"),   // Maps to addressFull
                rs.getLong("category_id"),      // Maps to categoryId
                rs.getString("author_email"),   // Maps to authorId
                rs.getDouble("calculated_rating"), // Maps to avgRating (Tricks app into showing live rating)
                rs.getString("cover_image_url")
        ));
    }

    // --- 2. Create Place ---
    @PostMapping("")
    public ResponseEntity<?> createPlace(@RequestBody PlaceRequest req) {
        try {
            Integer userId = jdbcTemplate.queryForObject(
                    "SELECT user_id FROM users WHERE email = ?", Integer.class, req.authorId
            );

            if (userId == null) return ResponseEntity.badRequest().body("User not found");
            String sql = """
            INSERT INTO places (name, description, lat, lng, address_full, category_id, user_id, status)
            VALUES (?, ?, ?, ?, ?, ?, ?, 'APPROVED')
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
                ps.setInt(7, userId);
                return ps;
            }, keyHolder);

            long newId = keyHolder.getKey().longValue();

            // Return response matching the new structure
            return ResponseEntity.ok(new PlaceResponse(
                    newId, req.name, req.description, req.lat, req.lng,
                    req.addressFull, req.categoryId, req.authorId, 0.0, null
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    // --- 3. Delete Place ---
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deletePlace(@PathVariable Long id) {
        String sql = "DELETE FROM places WHERE place_id = ?";
        int rows = jdbcTemplate.update(sql, id);
        if (rows > 0) return ResponseEntity.ok("Deleted");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Place not found");
    }
}