package com.Xplored.Xplored.Controller;

import com.Xplored.Xplored.Model.Coupon.CouponResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/coupons")
@CrossOrigin(origins = "*")
public class CouponController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // --- Endpoint 1: Get Active Coupons ---
    @GetMapping("/active")
    public List<CouponResponse> getActiveCoupons() {
        String sql = """
            SELECT 
                coupon_id, 
                title, 
                discount_type, 
                discount_value, 
                merchant_name, 
                terms,
                cost_points
            FROM coupons
            WHERE active = 1
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> new CouponResponse(
                rs.getLong("coupon_id"),
                rs.getString("title"),
                rs.getString("discount_type"),
                rs.getDouble("discount_value"),
                rs.getString("merchant_name"),
                rs.getString("terms"),
                rs.getInt("cost_points")
        ));
    }

    // --- Endpoint 2: Redeem Coupon ---
    public static class RedemptionRequest {
        public String userEmail;
        public Long couponId;
    }

    @PostMapping("/redeem")
    @Transactional
    public ResponseEntity<?> redeemCoupon(@RequestBody RedemptionRequest request) {
        try {
            // 1. Fetch User ID, Current Points, and Coupon Cost
            String sql = """
                SELECT u.user_id, u.points, c.cost_points 
                FROM users u 
                CROSS JOIN coupons c 
                WHERE u.email = ? AND c.coupon_id = ?
            """;

            List<Map<String, Object>> result = jdbcTemplate.query(sql, (rs, rowNum) -> Map.of(
                    "userId", rs.getInt("user_id"),
                    "points", rs.getInt("points"),
                    "cost", rs.getInt("cost_points")
            ), request.userEmail, request.couponId);

            if (result.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User or Coupon not found.");
            }

            Map<String, Object> data = result.get(0);
            int userId = (int) data.get("userId");
            int currentPoints = (int) data.get("points");
            int cost = (int) data.get("cost");

            // 2. Check balance
            if (currentPoints < cost) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Pontos insuficientes.");
            }

            // 3. Deduct Points
            jdbcTemplate.update("UPDATE users SET points = points - ? WHERE user_id = ?", cost, userId);

            // 4. Record Redemption
            jdbcTemplate.update("INSERT INTO redemptions (coupon_id, user_id) VALUES (?, ?)", request.couponId, userId);

            // 5. Add to Ledger
            jdbcTemplate.update("""
                INSERT INTO points_ledger (user_id, source_type, source_id, delta_points, note) 
                VALUES (?, 'REDEMPTION', ?, ?, 'Coupon Activation')
            """, userId, request.couponId, -cost);

            return ResponseEntity.ok(Map.of("message", "Success", "newBalance", currentPoints - cost));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing redemption.");
        }
    }
}