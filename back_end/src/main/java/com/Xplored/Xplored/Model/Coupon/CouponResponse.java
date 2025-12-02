package com.Xplored.Xplored.Model.Coupon;

public class CouponResponse {
    public Long id;
    public String title;
    public String description; // Real description from the coupons table
    public String code;        // Only filled in after redeeming
    public String merchant;
    public String details;     // No source column yet
    public int cost;

    public CouponResponse(Long id, String title, String description, String merchant, int cost) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.merchant = merchant;
        this.cost = cost;
    }
}
