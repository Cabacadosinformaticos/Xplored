package com.Xplored.Xplored.Controller;

import com.Xplored.Xplored.Model.Reaction.Reaction;
import com.Xplored.Xplored.Model.Reaction.ReactionRepository;
import com.Xplored.Xplored.Model.User;
import com.Xplored.Xplored.Model.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/reactions")
@CrossOrigin(origins = "*")
public class ReactionController {

    @Autowired
    private ReactionRepository reactionRepository;

    @Autowired
    private UserRepository userRepository;

    // Helper DTO for the request
    public static class ReactionRequest {
        public String userEmail;
        public Long reviewId;
        public String type; // "USEFUL" or "NOT_USEFUL"
    }

    @PostMapping("/toggle")
    public ResponseEntity<?> toggleReaction(@RequestBody ReactionRequest request) {
        // 1. Find User
        Optional<User> userOpt = userRepository.findByEmail(request.userEmail);
        if (userOpt.isEmpty()) return ResponseEntity.badRequest().body("User not found");
        Long userId = userOpt.get().getId();

        // 2. Check if reaction already exists
        Optional<Reaction> existing = reactionRepository.findByUserIdAndReviewId(userId, request.reviewId);

        if (existing.isPresent()) {
            Reaction r = existing.get();
            if (r.getReactionType().equals(request.type)) {
                // Same reaction clicked again -> DELETE (Toggle off)
                reactionRepository.delete(r);
                return ResponseEntity.ok("Removed");
            } else {
                // Different reaction -> UPDATE (Switch from Like to Dislike)
                r.setReactionType(request.type);
                reactionRepository.save(r);
                return ResponseEntity.ok("Updated");
            }
        } else {
            // New reaction -> CREATE
            Reaction newReaction = new Reaction();
            newReaction.setUserId(userId);
            newReaction.setReviewId(request.reviewId);
            newReaction.setReactionType(request.type);
            reactionRepository.save(newReaction);
            return ResponseEntity.ok("Created");
        }
    }
}