package com.Xplored.Xplored.Model.Pedipaper;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "pedipapers")
public class Pedipaper {

    // FIX: Map Java 'pediId' to Database column 'id'
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long pediId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "total_points", nullable = false)
    private Integer totalPoints = 0;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public Pedipaper() {}

    public Long getPediId() { return pediId; }
    public void setPediId(Long pediId) { this.pediId = pediId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Integer getTotalPoints() { return totalPoints; }
    public void setTotalPoints(Integer totalPoints) { this.totalPoints = totalPoints; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}