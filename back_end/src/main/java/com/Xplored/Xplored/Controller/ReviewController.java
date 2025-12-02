package com.Xplored.Xplored.Controller;

import com.Xplored.Xplored.Model.Photo.Photo;
import com.Xplored.Xplored.Model.Photo.PhotoRepository;
import com.Xplored.Xplored.Model.Place.Place;
import com.Xplored.Xplored.Model.Place.PlaceRepository;
import com.Xplored.Xplored.Model.Reaction.Reaction;
import com.Xplored.Xplored.Model.Reaction.ReactionRepository;
import com.Xplored.Xplored.Model.Review.Review;
import com.Xplored.Xplored.Model.Review.ReviewRepository;
import com.Xplored.Xplored.Model.Review.ReviewRequest;
import com.Xplored.Xplored.Model.Review.ReviewResponse;
import com.Xplored.Xplored.Model.User;
import com.Xplored.Xplored.Model.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/reviews")
@CrossOrigin(origins = "*")
public class ReviewController {

    private static final String GALLERY = "GALLERY";

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReactionRepository reactionRepository;

    @Autowired
    private PhotoRepository photoRepository;

    @Autowired
    private PlaceRepository placeRepository; // Needed to get Place Name for profile

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @PostMapping
    public ResponseEntity<?> createReview(@RequestBody ReviewRequest request) {
        Optional<User> userOpt = userRepository.findByEmail(request.userEmail);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        }
        if (request.rating < 1 || request.rating > 5) {
            return ResponseEntity.badRequest().body(Map.of("error", "Rating must be between 1 and 5"));
        }
        if (request.placeId == null || !placeRepository.existsById(request.placeId)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Place not found"));
        }
        if (request.title != null && request.title.length() > 100) {
            return ResponseEntity.badRequest().body(Map.of("error", "Title is too long (max 100 characters)"));
        }

        User user = userOpt.get();

        Review review = new Review();
        review.setUserId(user.getId());
        review.setPlaceId(request.placeId);
        review.setRating(request.rating);
        review.setTitle(request.title);
        review.setComment(request.comment);
        review.setStatus("APPROVED");

        Review saved = reviewRepository.save(review);

        // Recompute the place average from its reviews.
        jdbcTemplate.update(
                "UPDATE places SET avg_rating = (SELECT COALESCE(AVG(rating),0) FROM reviews WHERE place_id=?) WHERE id=?",
                request.placeId, request.placeId);

        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/by-place/{placeId}")
    public List<ReviewResponse> getByPlace(
            @PathVariable Long placeId,
            @RequestParam(required = false) String userEmail
    ) {
        List<Review> reviews = reviewRepository.findByPlaceId(placeId);

        // Fetch the place once to pass to responses (optimization)
        Place place = placeRepository.findById(placeId).orElse(null);
        String placeCoverUrl = coverFor(placeId);

        Long currentUserId = null;
        if (userEmail != null && !userEmail.isEmpty()) {
            Optional<User> u = userRepository.findByEmail(userEmail);
            if (u.isPresent()) currentUserId = u.get().getId();
        }
        final Long finalUserId = currentUserId;

        return reviews.stream().map(r -> {
            User author = userRepository.findById(r.getUserId()).orElse(null);
            List<Reaction> reactions = reactionRepository.findByReviewId(r.getReviewId());
            List<String> photoUrls = photoRepository.findByReviewId(r.getReviewId())
                    .stream()
                    .map(Photo::getUrl)
                    .collect(Collectors.toList());

            return new ReviewResponse(r, author, place, placeCoverUrl, reactions, finalUserId, photoUrls);
        }).collect(Collectors.toList());
    }

    // Fetch reviews for the Profile Screen
    @GetMapping("/by-user")
    public List<ReviewResponse> getByUser(@RequestParam String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) return List.of();

        User user = userOpt.get();
        List<Review> reviews = reviewRepository.findByUserId(user.getId());

        // When viewing your own profile, currentUserId is your own ID
        Long currentUserId = user.getId();

        return reviews.stream().map(r -> {
            // Fetch Place details for this specific review
            Place place = placeRepository.findById(r.getPlaceId()).orElse(null);

            List<Reaction> reactions = reactionRepository.findByReviewId(r.getReviewId());
            List<String> photoUrls = photoRepository.findByReviewId(r.getReviewId())
                    .stream()
                    .map(Photo::getUrl)
                    .collect(Collectors.toList());

            return new ReviewResponse(r, user, place, coverFor(r.getPlaceId()), reactions, currentUserId, photoUrls);
        }).collect(Collectors.toList());
    }

    // First GALLERY photo of a place, or null when it has none.
    private String coverFor(Long placeId) {
        if (placeId == null) return null;
        return photoRepository.findFirstByPlaceIdAndKindOrderByPhotoIdAsc(placeId, GALLERY)
                .map(Photo::getUrl)
                .orElse(null);
    }
}
