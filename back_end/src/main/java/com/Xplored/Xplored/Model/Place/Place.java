// src/main/java/com/xplored/model/Place.java
package com.Xplored.Xplored.Model.Place;

import com.Xplored.Xplored.Model.UserDao;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "places")
public class Place {

    // matches column: place_id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "place_id")
    private Long placeId;

    @Column(name = "name")
    private String name;

    // matches column: description
    @Column(name = "description")
    private String description;

    // matches column: lat
    @Column(name = "lat")
    private Double lat;

    // matches column: lng
    @Column(name = "lng")
    private Double lng;

    // matches column: address_full
    @Column(name = "address_full")
    private String addressFull;

    // matches column: postal_code
    @Column(name = "postal_code")
    private String postalCode;

    // --- the DB image also showed other columns (optional to store) ---
    // If you want to keep them in the entity later, they are included but optional.
    @Column(name = "avg_rating")
    private Double avgRating;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "status")
    private String status;

    @Column(name = "cover_image_url")
    private String coverImageUrl;


    public Place() {}

    public Long getPlaceId() {
        return placeId;
    }

    public void setPlaceId(Long placeId) {
        this.placeId = placeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public String getAddressFull() {
        return addressFull;
    }

    public void setAddressFull(String addressFull) {
        this.addressFull = addressFull;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public Double getAvgRating() {
        return avgRating;
    }

    public void setAvgRating(Double avgRating) {
        this.avgRating = avgRating;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }

    public Place(String name, String description, double lat, double lng,
                 String addressFull, String postalCode, Double avgRating,
                 Long categoryId, String status) {

        this.name = name;
        this.description = description;
        this.lat = lat;
        this.lng = lng;
        this.addressFull = addressFull;
        this.postalCode = postalCode;
        this.avgRating = avgRating;
        this.categoryId = categoryId;
        this.status = status;



    }
}
