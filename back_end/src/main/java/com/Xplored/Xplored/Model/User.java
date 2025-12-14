package com.Xplored.Xplored.Model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id") // Fixed: DB uses 'id'
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "role")
    private String role = "USER";

    @Column(name = "country")
    private String country;

    @Column(name = "points")
    private int points = 0;

    @Column(name = "profile_photo") // Fixed: DB column is 'profile_photo'
    private String profilePhotoUrl;

    @Column(name = "about")
    private String about;

    @Column(name = "created_at", updatable = false, insertable = false)
    private LocalDateTime createdAt;

    public User() {}

    // Only essentials for registration
    public User(String name, String email, String passwordHash) {
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = "USER";
        this.points = 0;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public int getPoints() { return points; }
    public void setPoints(int points) { this.points = points; }

    public String getProfilePhotoUrl() { return profilePhotoUrl; }
    public void setProfilePhotoUrl(String profilePhotoUrl) { this.profilePhotoUrl = profilePhotoUrl; }

    public String getAbout() { return about; }
    public void setAbout(String about) { this.about = about; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}