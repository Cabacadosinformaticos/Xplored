package com.Xplored.Xplored.Model.Place;

import java.util.List;

public class PlaceResponse {
    public Long placeId;          // Changed to Long
    public String name;
    public String description;
    public double lat;
    public double lng;
    public String addressFull;    // MATCHES ANDROID
    public Long categoryId;       // MATCHES ANDROID
    public String authorId;       // MATCHES ANDROID
    public double avgRating;      // MATCHES ANDROID
    public String coverImageUrl;  // MATCHES ANDROID

    public PlaceResponse(Long placeId, String name, String description, double lat, double lng,
                         String addressFull, Long categoryId, String authorId, double avgRating, String coverImageUrl) {
        this.placeId = placeId;
        this.name = name;
        this.description = description;
        this.lat = lat;
        this.lng = lng;
        this.addressFull = addressFull;
        this.categoryId = categoryId;
        this.authorId = authorId;
        this.avgRating = avgRating;
        this.coverImageUrl = coverImageUrl;
    }
}