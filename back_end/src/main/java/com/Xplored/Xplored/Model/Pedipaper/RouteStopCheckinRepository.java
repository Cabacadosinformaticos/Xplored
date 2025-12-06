package com.Xplored.Xplored.Model.Pedipaper;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RouteStopCheckinRepository extends JpaRepository<RouteStopCheckin, Long> {

    List<RouteStopCheckin> findByParticipationId(Long participationId);

    Optional<RouteStopCheckin> findByParticipationIdAndRouteStopId(Long participationId, Long routeStopId);

    long countByParticipationId(Long participationId);
}
