package com.Xplored.Xplored.Model.Coupon;
import java.util.UUID;

public class CouponResponse {
    public Long id;
    public String title;
    public String description; // Generated from discount type/value
    public String code;        // We will generate a code string
    public String merchant;
    public String details;     // Terms
    public int cost;

    public CouponResponse(Long id, String title, String type, Double value, String merchant, String terms, int cost) {
        this.id = id;
        this.title = title;
        this.merchant = merchant;
        this.details = terms;
        this.cost = cost;

        // OLD: this.code = "CPN-" + id + "-" + cost;
        // NEW: Generates a random 8-character code like "XPL-9F3A12"
        this.code = "XPL-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        if ("PERCENT".equals(type)) {
            this.description = String.format("Desconto de %.0f%% na sua compra.", value);
        } else {
            this.description = String.format("Vale de %.2f€ em compras.", value);
        }
    }
}