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
        // Updated SQL for new Schema:
        // 1. Uses 'id' instead of 'place_id'
        // 2. Uses 'author_id' instead of 'user_id'
        // 3. Selects 'avg_rating' column instead of live calculation
        // 4. Subquery for cover image (since cover_image_url column is gone)
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
                -- Get the first 'GALLERY' photo as cover
                (SELECT ph.url FROM photos ph WHERE ph.place_id = p.id AND ph.kind = 'GALLERY' LIMIT 1) AS cover_image_url
            FROM places p
            LEFT JOIN users u ON p.author_id = u.id
            -- Optional: Filter by verified if you strictly want approved places
            -- WHERE p.is_verified = TRUE 
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> new PlaceResponse(
                rs.getLong("place_id"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getDouble("lat"),
                rs.getDouble("lng"),
                rs.getString("address_full"),
                rs.getLong("category_id"),
                rs.getString("author_email"), // Returns email as authorId for frontend logic
                rs.getDouble("avg_rating"),
                rs.getString("cover_image_url")
        ));
    }

    // --- 2. Create Place ---
    @PostMapping("")
    public ResponseEntity<?> createPlace(@RequestBody PlaceRequest req) {
        try {
            // 1. Resolve author email to Database ID
            Integer userId = jdbcTemplate.queryForObject(
                    "SELECT id FROM users WHERE email = ?", Integer.class, req.authorId
            );

            if (userId == null) return ResponseEntity.badRequest().body("User not found");

            // 2. Insert into new schema
            // Note: We default avg_rating to 0.0 and is_verified to TRUE (or FALSE if you want moderation)
            String sql = """
            INSERT INTO places (name, description, lat, lng, address_full, category_id, author_id, avg_rating, is_verified)
            VALUES (?, ?, ?, ?, ?, ?, ?, 0.0, TRUE)
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

            // Return success response matching frontend model
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
        // Updated WHERE clause to use 'id'
        String sql = "DELETE FROM places WHERE id = ?";
        int rows = jdbcTemplate.update(sql, id);
        if (rows > 0) return ResponseEntity.ok("Deleted");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Place not found");
    }
}