package com.Xplored.Xplored.Model.Pedipaper;

import jakarta.persistence.*;

@Entity
@Table(name = "route_stops")
public class RouteStop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stop_id")
    private Long stopId;

    @Column(name = "pedi_id", nullable = false)
    private Long pediId;

    @Column(name = "place_id", nullable = false)
    private Long placeId;

    @Column(name = "order_num", nullable = false)
    private Integer orderNum;

    @Column(name = "requires_photo", nullable = false)
    private Boolean requiresPhoto = false;

    @Column(name = "task_description", columnDefinition = "TEXT")
    private String taskDescription;

    public RouteStop() {}

    // Getters & Setters
    public Long getStopId() { return stopId; }
    public void setStopId(Long stopId) { this.stopId = stopId; }

    public Long getPediId() { return pediId; }
    public void setPediId(Long pediId) { this.pediId = pediId; }

    public Long getPlaceId() { return placeId; }
    public void setPlaceId(Long placeId) { this.placeId = placeId; }

    public Integer getOrderNum() { return orderNum; }
    public void setOrderNum(Integer orderNum) { this.orderNum = orderNum; }

    public Boolean getRequiresPhoto() { return requiresPhoto; }
    public void setRequiresPhoto(Boolean requiresPhoto) { this.requiresPhoto = requiresPhoto; }

    public String getTaskDescription() { return taskDescription; }
    public void setTaskDescription(String taskDescription) { this.taskDescription = taskDescription; }
}