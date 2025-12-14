package com.Xplored.Xplored.Controller;

import com.Xplored.Xplored.Model.Pedipaper.*;
import com.Xplored.Xplored.Model.Place.Place;
import com.Xplored.Xplored.Model.Place.PlaceRepository;
import com.Xplored.Xplored.Model.User;
import com.Xplored.Xplored.Model.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/pedipapers")
@CrossOrigin(origins = "*")
public class PedipaperController {

    @Autowired
    private PedipaperRepository pedipaperRepository;

    @Autowired
    private RouteStopRepository routeStopRepository;

    @Autowired
    private PlaceRepository placeRepository;

    @Autowired
    private RouteParticipationRepository participationRepository;

    @Autowired
    private UserRepository userRepository;

    // 1. Get all Active Pedipapers
    @GetMapping
    public List<Pedipaper> getAllPedipapers() {
        return pedipaperRepository.findAll();
    }

    // 2. Get the specific stops (with Place details) for a Pedipaper
    // We create a custom DTO class inside here for simplicity
    @GetMapping("/{id}/stops")
    public ResponseEntity<List<StopResponse>> getStops(@PathVariable Long id) {
        List<RouteStop> stops = routeStopRepository.findByPediIdOrderByOrderNumAsc(id);

        List<StopResponse> response = new ArrayList<>();
        for (RouteStop stop : stops) {
            Optional<Place> placeOpt = placeRepository.findById(stop.getPlaceId());
            placeOpt.ifPresent(place -> response.add(new StopResponse(stop, place)));
        }
        return ResponseEntity.ok(response);
    }

    // 3. Start a Pedipaper (Join)
    @PostMapping("/{id}/join")
    public ResponseEntity<?> joinPedipaper(@PathVariable Long id, @RequestParam String userEmail) {
        Optional<User> userOpt = userRepository.findByEmail(userEmail);
        if (userOpt.isEmpty()) return ResponseEntity.badRequest().body("User not found");

        Long userId = userOpt.get().getId();

        // Check if already joined
        Optional<RouteParticipation> existing = participationRepository.findByUserIdAndPediId(userId, id);
        if (existing.isPresent()) {
            return ResponseEntity.ok("Already joined");
        }

        RouteParticipation rp = new RouteParticipation();
        rp.setPediId(id);
        rp.setUserId(userId);
        // started_at is handled by DB default, or we can set it explicitly if needed
        participationRepository.save(rp);

        return ResponseEntity.ok("Joined successfully");
    }

    // 4. Complete a Pedipaper (Award points)
    // In a real app, you'd check if all stops are verified.
    // For this project, we call this when the user finishes the last stop.
    @PostMapping("/{id}/complete")
    public ResponseEntity<?> completePedipaper(@PathVariable Long id, @RequestParam String userEmail) {
        Optional<User> userOpt = userRepository.findByEmail(userEmail);
        if (userOpt.isEmpty()) return ResponseEntity.badRequest().body("User not found");

        User user = userOpt.get();
        Optional<RouteParticipation> rpOpt = participationRepository.findByUserIdAndPediId(user.getId(), id);

        if (rpOpt.isPresent()) {
            RouteParticipation rp = rpOpt.get();
            if (rp.getCompletedAt() == null) {
                // Fetch route to get total points
                Optional<Pedipaper> pediOpt = pedipaperRepository.findById(id);
                int points = pediOpt.map(Pedipaper::getTotalPoints).orElse(0);

                rp.setCompletedAt(LocalDateTime.now());
                rp.setPointsAwarded(points);
                participationRepository.save(rp);

                // Add points to User profile
                user.setPoints(user.getPoints() + points);
                userRepository.save(user);

                return ResponseEntity.ok("Completed! Points awarded: " + points);
            }
            return ResponseEntity.ok("Already completed");
        }
        return ResponseEntity.badRequest().body("Not participating in this route");
    }

    // Helper DTO class
    public static class StopResponse {
        public Long stopId;
        public int orderNum;
        public String taskDescription;
        public boolean requiresPhoto;

        // Flattened Place Data
        public Long placeId;
        public String placeName;
        public Double lat;
        public Double lng;
        public String placeCoverUrl;

        public StopResponse(RouteStop stop, Place place) {
            this.stopId = stop.getStopId();
            this.orderNum = stop.getOrderNum();
            this.taskDescription = stop.getTaskDescription();
            this.requiresPhoto = stop.getRequiresPhoto();

            this.placeId = place.getPlaceId();
            this.placeName = place.getName();
            this.lat = place.getLat();
            this.lng = place.getLng();
            this.placeCoverUrl = place.getCoverImageUrl();
        }
    }
}