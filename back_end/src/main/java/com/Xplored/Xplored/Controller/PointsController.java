package com.Xplored.Xplored.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.List;

@RestController
@RequestMapping("/user")
@CrossOrigin(origins = "*")
public class PointsController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // One movement of the points ledger.
    public record PointsEntry(int amount, String reason, String createdAt) {}

    // --- Endpoint: points movements of a user, newest first ---
    @GetMapping("/points-history")
    public List<PointsEntry> getPointsHistory(@RequestParam String email) {
        String sql = """
            SELECT pl.amount, pl.reason, pl.created_at
            FROM points_ledger pl
            JOIN users u ON pl.user_id = u.id
            WHERE u.email = ?
            ORDER BY pl.created_at DESC, pl.id DESC
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Timestamp createdAt = rs.getTimestamp("created_at");
            return new PointsEntry(
                    rs.getInt("amount"),
                    rs.getString("reason"),
                    createdAt == null ? null : createdAt.toString()
            );
        }, email);
    }
}
