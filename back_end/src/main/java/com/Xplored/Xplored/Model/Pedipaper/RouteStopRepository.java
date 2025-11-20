package com.Xplored.Xplored.Model.Pedipaper;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RouteStopRepository extends JpaRepository<RouteStop, Long> {
    // Fetch stops for a specific route, ordered by sequence
    List<RouteStop> findByPediIdOrderByOrderNumAsc(Long pediId);
}