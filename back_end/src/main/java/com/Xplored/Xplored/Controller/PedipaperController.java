package com.Xplored.Xplored.Controller;

import com.Xplored.Xplored.Config.UrlUtil;
import com.Xplored.Xplored.Model.Pedipaper.*;
import com.Xplored.Xplored.Model.Photo.Photo;
import com.Xplored.Xplored.Model.Photo.PhotoRepository;
import com.Xplored.Xplored.Model.Place.Place;
import com.Xplored.Xplored.Model.Place.PlaceRepository;
import com.Xplored.Xplored.Model.User;
import com.Xplored.Xplored.Model.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/pedipapers")
@CrossOrigin(origins = "*")
public class PedipaperController {

    private static final String GALLERY = "GALLERY";

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

    @Autowired
    private PhotoRepository photoRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 1. Get all Active Pedipapers
    @GetMapping
    public List<Pedipaper> getAllPedipapers() {
        return pedipaperRepository.findByActiveTrue();
    }

    // 2. Get the specific stops (with Place details) for a Pedipaper
    @GetMapping("/{id}/stops")
    public ResponseEntity<List<StopResponse>> getStops(@PathVariable Long id) {
        List<RouteStop> stops = routeStopRepository.findByPediIdOrderByOrderNumAsc(id);

        List<StopResponse> response = new ArrayList<>();
        for (RouteStop stop : stops) {
            Optional<Place> placeOpt = placeRepository.findById(stop.getPlaceId());
            placeOpt.ifPresent(place ->
                    response.add(new StopResponse(stop, place, coverFor(place.getPlaceId()))));
        }
        return ResponseEntity.ok(response);
    }

    // 3. Start a Pedipaper (Join)
    @PostMapping("/{id}/join")
    public ResponseEntity<?> joinPedipaper(@PathVariable Long id, @RequestParam String userEmail) {
        Optional<User> userOpt = userRepository.findByEmail(userEmail);
        if (userOpt.isEmpty()) return ResponseEntity.badRequest().body(Map.of("error", "User not found"));

        if (!pedipaperRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Pedipaper not found"));
        }

        Long userId = userOpt.get().getId();

        Optional<RouteParticipation> existing = participationRepository.findByUserIdAndPediId(userId, id);
        if (existing.isPresent()) {
            return ResponseEntity.ok(Map.of("message", "Already joined"));
        }

        RouteParticipation rp = new RouteParticipation();
        rp.setPediId(id);
        rp.setUserId(userId);
        rp.setCompleted(false);
        rp.setPointsAwarded(0);
        participationRepository.save(rp);

        return ResponseEntity.ok(Map.of("message", "Joined successfully"));
    }

    // 4. Complete a Pedipaper (award points)
    @Transactional
    @PostMapping("/{id}/complete")
    public ResponseEntity<?> completePedipaper(@PathVariable Long id, @RequestParam String userEmail) {
        Optional<User> userOpt = userRepository.findByEmail(userEmail);
        if (userOpt.isEmpty()) return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        User user = userOpt.get();

        Optional<Pedipaper> pediOpt = pedipaperRepository.findById(id);
        if (pediOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Pedipaper not found"));
        }

        Optional<RouteParticipation> rpOpt = participationRepository.findByUserIdAndPediId(user.getId(), id);
        if (rpOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Not participating in this route"));
        }
        RouteParticipation rp = rpOpt.get();
        if (Boolean.TRUE.equals(rp.getCompleted())) {
            return ResponseEntity.ok(Map.of("message", "Already completed"));
        }

        int points = pediOpt.get().getTotalPoints() == null ? 0 : pediOpt.get().getTotalPoints();

        rp.setCompleted(true);
        rp.setCompletedAt(LocalDateTime.now());
        rp.setPointsAwarded(points);
        participationRepository.save(rp);

        // Atomic points update plus a ledger entry.
        jdbcTemplate.update("UPDATE users SET points = points + ? WHERE id = ?", points, user.getId());
        jdbcTemplate.update(
                "INSERT INTO points_ledger (user_id, amount, reason) VALUES (?, ?, ?)",
                user.getId(), points, "Completed pedipaper " + id);

        return ResponseEntity.ok(Map.of(
                "message", "Completed! Points awarded: " + points,
                "pointsAwarded", points));
    }

    // First GALLERY photo of a place, or null when it has none.
    private String coverFor(Long placeId) {
        if (placeId == null) return null;
        return photoRepository.findFirstByPlaceIdAndKindOrderByPhotoIdAsc(placeId, GALLERY)
                .map(Photo::getUrl)
                .orElse(null);
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

        public StopResponse(RouteStop stop, Place place, String placeCoverUrl) {
            this.stopId = stop.getStopId();
            this.orderNum = stop.getOrderNum();
            this.taskDescription = stop.getTaskDescription();
            this.requiresPhoto = stop.getRequiresPhoto();

            this.placeId = place.getPlaceId();
            this.placeName = place.getName();
            this.lat = place.getLat();
            this.lng = place.getLng();
            this.placeCoverUrl = UrlUtil.toAbsolute(placeCoverUrl);
        }
    }
}
