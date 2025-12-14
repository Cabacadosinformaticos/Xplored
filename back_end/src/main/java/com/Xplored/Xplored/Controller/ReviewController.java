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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/reviews")
@CrossOrigin(origins = "*")
public class ReviewController {

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

    @PostMapping
    public ResponseEntity<?> createReview(@RequestBody ReviewRequest request) {
        // 1. Find the user by Email
        Optional<User> userOpt = userRepository.findByEmail(request.userEmail);

        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body("User not found with email: " + request.userEmail);
        }

        User user = userOpt.get();

        // 2. Create the Review Entity
        Review review = new Review();
        review.setUserId(user.getId());
        review.setPlaceId(request.placeId);
        review.setRating(request.rating);
        review.setTitle(request.title);
        review.setComment(request.comment);
        review.setStatus("APPROVED");

        Review saved = reviewRepository.save(review);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/by-place/{placeId}")
    public List<ReviewResponse> getByPlace(
            @PathVariable Long placeId,
            @RequestParam(required = false) String userEmail
    ) {
        List<Review> reviews = reviewRepository.findByPlaceId(placeId);

        // Fetch the place once to pass to responses (optimization)
        Place place = placeRepository.findById(placeId).orElse(null);

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

            // Pass 'place' to the new constructor
            return new ReviewResponse(r, author, place, reactions, finalUserId, photoUrls);
        }).collect(Collectors.toList());
    }

    // NEW ENDPOINT: Fetch reviews for the Profile Screen
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

            return new ReviewResponse(r, user, place, reactions, currentUserId, photoUrls);
        }).collect(Collectors.toList());
    }
}