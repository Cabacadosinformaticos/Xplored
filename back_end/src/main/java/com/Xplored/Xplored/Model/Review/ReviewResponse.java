package com.Xplored.Xplored.Model.Review;

import com.Xplored.Xplored.Config.UrlUtil;
import com.Xplored.Xplored.Model.Place.Place;
import com.Xplored.Xplored.Model.Reaction.Reaction;
import com.Xplored.Xplored.Model.User;

import java.util.List;
import java.util.stream.Collectors;

public class ReviewResponse {
    public Long reviewId;
    public Long userId;
    public Long placeId;
    public Integer rating;
    public String title;
    public String comment;
    public String createdAt;

    // User details
    public String userName;
    public String userPhotoUrl;

    // NEW: Place details (Required for Profile Screen)
    public String placeName;
    public String placeCoverUrl;

    // Reactions
    public int likesCount;
    public int dislikesCount;
    public String currentUserReaction; // "USEFUL", "NOT_USEFUL", or null

    // Photos associated with the review
    public List<String> reviewPhotoUrls;

    // Updated Constructor to accept 'Place' and its computed cover URL
    public ReviewResponse(Review review, User user, Place place, String placeCoverUrl,
                          List<Reaction> reactions, Long currentUserId, List<String> photoUrls) {
        this.reviewId = review.getReviewId();
        this.userId = review.getUserId();
        this.placeId = review.getPlaceId();
        this.rating = review.getRating();
        this.title = review.getTitle();
        this.comment = review.getComment();

        // Assign photos
        this.reviewPhotoUrls = photoUrls == null ? null
                : photoUrls.stream().map(UrlUtil::toAbsolute).collect(Collectors.toList());

        if (review.getCreatedAt() != null) {
            this.createdAt = review.getCreatedAt().toString();
        }

        // Fill User Info
        if (user != null) {
            this.userName = user.getName();
            this.userPhotoUrl = UrlUtil.toAbsolute(user.getProfilePhotoUrl());
        } else {
            this.userName = "Utilizador Xplored";
            this.userPhotoUrl = null;
        }

        // Fill Place Info (cover computed from the first GALLERY photo)
        if (place != null) {
            this.placeName = place.getName();
        }
        this.placeCoverUrl = UrlUtil.toAbsolute(placeCoverUrl);

        // Fill Reactions
        if (reactions != null) {
            this.likesCount = (int) reactions.stream().filter(r -> "USEFUL".equals(r.getReactionType())).count();
            this.dislikesCount = (int) reactions.stream().filter(r -> "NOT_USEFUL".equals(r.getReactionType())).count();

            if (currentUserId != null) {
                this.currentUserReaction = reactions.stream()
                        .filter(r -> r.getUserId().equals(currentUserId))
                        .map(Reaction::getReactionType)
                        .findFirst()
                        .orElse(null);
            }
        }
    }
}