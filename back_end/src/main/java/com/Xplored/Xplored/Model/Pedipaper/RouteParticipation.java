package com.Xplored.Xplored.Model.Pedipaper;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "route_participations")
public class RouteParticipation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "participation_id")
    private Long participationId;

    @Column(name = "pedi_id", nullable = false)
    private Long pediId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "started_at", insertable = false, updatable = false)
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "points_awarded")
    private Integer pointsAwarded;

    public RouteParticipation() {}

    // Getters & Setters
    public Long getParticipationId() { return participationId; }
    public void setParticipationId(Long participationId) { this.participationId = participationId; }

    public Long getPediId() { return pediId; }
    public void setPediId(Long pediId) { this.pediId = pediId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public LocalDateTime getStartedAt() { return startedAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public Integer getPointsAwarded() { return pointsAwarded; }
    public void setPointsAwarded(Integer pointsAwarded) { this.pointsAwarded = pointsAwarded; }
}