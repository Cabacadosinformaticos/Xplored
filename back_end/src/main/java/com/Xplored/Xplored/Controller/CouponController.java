package com.Xplored.Xplored.Controller;

import com.Xplored.Xplored.Model.Coupon.CouponResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.ResponseEntity;
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
        // Fix: Join with business_accounts to get the name and use the correct column 'id'
        String sql = """
            SELECT 
                c.id, 
                c.title, 
                c.cost_points, 
                c.description,
                b.name as merchant_name
            FROM coupons c
            JOIN business_accounts b ON c.business_account_id = b.id
            WHERE c.active = TRUE
        """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> new CouponResponse(
                rs.getLong("id"),                // DB Column: id
                rs.getString("title"),           // DB Column: title
                "VOUCHER",                       // Default type (DB column missing)
                0.0,                             // Default value (DB column missing)
                rs.getString("merchant_name"),   // Joined Column
                "Válido na loja física.",        // Default Terms
                rs.getInt("cost_points")         // DB Column: cost_points
        ));
    }

    // --- Endpoint 2: Redeem Coupon ---
    @PostMapping("/redeem")
    public ResponseEntity<?> redeemCoupon(@RequestBody Map<String, Object> payload) {
        String userEmail = (String) payload.get("userEmail");
        Number couponIdNum = (Number) payload.get("couponId");
        Long couponId = couponIdNum.longValue();

        try {
            // 1. Fetch User ID & Points (DB uses 'id', not 'user_id')
            List<Map<String, Object>> users = jdbcTemplate.query(
                    "SELECT id, points FROM users WHERE email = ?",
                    (rs, rowNum) -> Map.of("id", rs.getInt("id"), "points", rs.getInt("points")),
                    userEmail
            );

            if (users.isEmpty()) return ResponseEntity.badRequest().body("User not found");

            Map<String, Object> user = users.get(0);
            int userId = (int) user.get("id");
            int currentPoints = (int) user.get("points");

            // 2. Fetch Coupon Cost (DB uses 'id')
            Integer cost = jdbcTemplate.queryForObject(
                    "SELECT cost_points FROM coupons WHERE id = ?", Integer.class, couponId
            );

            if (cost == null) return ResponseEntity.badRequest().body("Coupon not found");
            if (currentPoints < cost) return ResponseEntity.badRequest().body("Pontos insuficientes.");

            // 3. Deduct Points
            jdbcTemplate.update("UPDATE users SET points = points - ? WHERE id = ?", cost, userId);

            // 4. Record Redemption
            jdbcTemplate.update("INSERT INTO redemptions (user_id, coupon_id) VALUES (?, ?)", userId, couponId);

            // 5. Add to Ledger
            jdbcTemplate.update(
                    "INSERT INTO points_ledger (user_id, amount, reason) VALUES (?, ?, ?)",
                    userId, -cost, "Redeemed Coupon " + couponId
            );

            return ResponseEntity.ok(Map.of("message", "Success", "newBalance", currentPoints - cost));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}