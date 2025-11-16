package com.Xplored.Xplored.Model.Review;

public class ReviewRequest {
    public String userEmail; // Incoming email
    public Long placeId;
    public int rating;
    public String title;
    public String comment;
    public boolean isVerifiedCustomer;
    public String status;
}