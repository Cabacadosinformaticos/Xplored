package com.Xplored.Xplored.Model.Photo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PhotoRepository extends JpaRepository<Photo, Long> {
    List<Photo> findByPlaceIdAndKind(Long placeId, String kind);
    List<Photo> findByUserIdAndKind(Long userId, String kind);
    List<Photo> findByReviewId(Long reviewId);
    List<Photo> findByUserId(Long userId);

    // First GALLERY photo of a place, used to build the cover.
    Optional<Photo> findFirstByPlaceIdAndKindOrderByPhotoIdAsc(Long placeId, String kind);
}
