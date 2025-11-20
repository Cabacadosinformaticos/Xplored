package com.Xplored.Xplored.Model.Pedipaper;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface RouteParticipationRepository extends JpaRepository<RouteParticipation, Long> {
    Optional<RouteParticipation> findByUserIdAndPediId(Long userId, Long pediId);

    List<RouteParticipation> findByUserId(Long userId);
}