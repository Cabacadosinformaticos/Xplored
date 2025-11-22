package com.Xplored.Xplored.Model.Place;

public class PlaceRequest {
    public String name;
    public String description;
    public double lat;
    public double lng;
    public String addressFull;
    public String coverImageUrl; // Added this just in case
    public Long categoryId;
    public String authorId; // This corresponds to the user's email
    public String status;
}