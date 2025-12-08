package com.Xplored.Xplored.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/visits")
@CrossOrigin(origins = "*")
public class VisitController {

    // A check-in only counts inside this radius of the place.
    private static final double MAX_VISIT_DISTANCE_METERS = 100.0;
    // Mean earth radius in metres, used by the haversine formula.
    private static final double EARTH_RADIUS_METERS = 6371000.0;
    private static final int POINTS_PER_VISIT = 10;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // --- 1. Check in at a place (GPS, once per place, +10 points) ---
    @Transactional
    @PostMapping("")
    public ResponseEntity<?> checkin(@RequestBody Map<String, Object> payload) {
        Object emailRaw = payload.get("userEmail");
        Object placeIdRaw = payload.get("placeId");
        Object latRaw = payload.get("lat");
        Object lngRaw = payload.get("lng");
        if (!(emailRaw instanceof String userEmail) || userEmail.isBlank()
                || !(placeIdRaw instanceof Number) || !(latRaw instanceof Number)
                || !(lngRaw instanceof Number)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "userEmail, placeId, lat and lng are required"));
        }
        Long placeId = ((Number) placeIdRaw).longValue();
        double lat = ((Number) latRaw).doubleValue();
        double lng = ((Number) lngRaw).doubleValue();

        Long userId = findUserId(userEmail);
        if (userId == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        }

        List<Map<String, Object>> places = jdbcTemplate.query(
                "SELECT name, lat, lng FROM places WHERE id = ?",
                (rs, rowNum) -> Map.of(
                        "name", rs.getString("name"),
                        "lat", rs.getDouble("lat"),
                        "lng", rs.getDouble("lng")),
                placeId);
        if (places.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Place not found"));
        }
        Map<String, Object> place = places.get(0);
        String placeName = (String) place.get("name");
        double placeLat = (Double) place.get("lat");
        double placeLng = (Double) place.get("lng");

        if (distanceMeters(lat, lng, placeLat, placeLng) > MAX_VISIT_DISTANCE_METERS) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Estás demasiado longe do local (máximo 100 m)"));
        }

        // Repeating a check-in is idempotent: no new row, no more points.
        if (count("SELECT COUNT(*) FROM visits WHERE user_id = ? AND place_id = ?", userId, placeId) > 0) {
            return ResponseEntity.ok(Map.of(
                    "pointsAwarded", 0,
                    "newBalance", balanceOf(userId),
                    "visitCount", countVisits(userId)));
        }

        jdbcTemplate.update(
                "INSERT INTO visits (user_id, place_id, lat, lng) VALUES (?, ?, ?, ?)",
                userId, placeId, lat, lng);
        jdbcTemplate.update("UPDATE users SET points = points + ? WHERE id = ?", POINTS_PER_VISIT, userId);
        jdbcTemplate.update(
                "INSERT INTO points_ledger (user_id, amount, reason) VALUES (?, ?, ?)",
                userId, POINTS_PER_VISIT, "Visita: " + placeName);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "pointsAwarded", POINTS_PER_VISIT,
                "newBalance", balanceOf(userId),
                "visitCount", countVisits(userId)));
    }

    // --- 2. How many places the user has visited ---
    @GetMapping("/count")
    public Map<String, Object> getVisitCount(@RequestParam String userEmail) {
        Long userId = findUserId(userEmail);
        int visits = userId == null ? 0 : countVisits(userId);
        return Map.of("visitCount", visits);
    }

    private Long findUserId(String email) {
        List<Long> ids = jdbcTemplate.query(
                "SELECT id FROM users WHERE email = ?",
                (rs, rowNum) -> rs.getLong("id"),
                email);
        return ids.isEmpty() ? null : ids.get(0);
    }

    private int balanceOf(Long userId) {
        Integer balance = jdbcTemplate.queryForObject(
                "SELECT points FROM users WHERE id = ?", Integer.class, userId);
        return balance == null ? 0 : balance;
    }

    private int countVisits(Long userId) {
        return count("SELECT COUNT(*) FROM visits WHERE user_id = ?", userId);
    }

    private int count(String sql, Object... args) {
        Integer value = jdbcTemplate.queryForObject(sql, Integer.class, args);
        return value == null ? 0 : value;
    }

    // Great circle distance in metres (haversine).
    private static double distanceMeters(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_METERS * c;
    }
}
