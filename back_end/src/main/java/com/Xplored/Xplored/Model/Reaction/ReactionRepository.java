package com.Xplored.Xplored.Model.Reaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface ReactionRepository extends JpaRepository<Reaction, Long> {
    Optional<Reaction> findByUserIdAndReviewId(Long userId, Long reviewId);
    List<Reaction> findByReviewId(Long reviewId);
}