package com.Xplored.Xplored.Model.Pedipaper;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "route_stop_checkins")
public class RouteStopCheckin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long checkinId;

    @Column(name = "participation_id", nullable = false)
    private Long participationId;

    @Column(name = "route_stop_id", nullable = false)
    private Long routeStopId;

    @Column(name = "lat", nullable = false)
    private Double lat;

    @Column(name = "lng", nullable = false)
    private Double lng;

    @Column(name = "photo_id")
    private Long photoId;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public RouteStopCheckin() {}

    public Long getCheckinId() { return checkinId; }
    public void setCheckinId(Long checkinId) { this.checkinId = checkinId; }

    public Long getParticipationId() { return participationId; }
    public void setParticipationId(Long participationId) { this.participationId = participationId; }

    public Long getRouteStopId() { return routeStopId; }
    public void setRouteStopId(Long routeStopId) { this.routeStopId = routeStopId; }

    public Double getLat() { return lat; }
    public void setLat(Double lat) { this.lat = lat; }

    public Double getLng() { return lng; }
    public void setLng(Double lng) { this.lng = lng; }

    public Long getPhotoId() { return photoId; }
    public void setPhotoId(Long photoId) { this.photoId = photoId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
}
