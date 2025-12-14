package com.Xplored.Xplored.Model.Place;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "places")
public class Place {

    // matches column: place_id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "place_id")
    private Long placeId;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    // matches column: description
    @Column(name = "description")
    private String description;

    // matches column: lat DECIMAL(9,6) NOT NULL
    @Column(name = "lat", nullable = false)
    private Double lat;

    // matches column: lng DECIMAL(9,6) NOT NULL
    @Column(name = "lng", nullable = false)
    private Double lng;

    // matches column: address_full
    @Column(name = "address_full", length = 255)
    private String addressFull;

    // matches column: postal_code
    @Column(name = "postal_code", length = 15)
    private String postalCode;

    // avg_rating DECIMAL(2,1) NULL
    @Column(name = "avg_rating")
    private Double avgRating;

    // FK to categories.category_id
    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    // ENUM('PENDING','APPROVED','REJECTED')
    @Column(name = "status", nullable = false, length = 20)
    private String status = "APPROVED";

    @Column(name = "cover_image_url", length = 255)
    private String coverImageUrl;

    // created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    // Let MySQL fill this automatically.
    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    private LocalDateTime createdAt;

    // ----- Constructors -----

    public Place() {}

    public Place(String name,
                 String description,
                 double lat,
                 double lng,
                 String addressFull,
                 String postalCode,
                 Double avgRating,
                 Long categoryId,
                 String status) {

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

    // ----- Getters / Setters -----

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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
