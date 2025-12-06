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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/pedipapers")
@CrossOrigin(origins = "*")
public class PedipaperController {

    private static final String GALLERY = "GALLERY";
    private static final String EVIDENCE = "EVIDENCE";
    // A check-in counts only when the user is inside this radius of the stop place.
    private static final double MAX_CHECKIN_DISTANCE_METERS = 50.0;
    // Mean earth radius in metres, used by the haversine formula.
    private static final double EARTH_RADIUS_METERS = 6371000.0;

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
    private RouteStopCheckinRepository checkinRepository;

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

    // 4. Check in at one stop of a Pedipaper (GPS + optional evidence photo)
    @Transactional
    @PostMapping("/{id}/stops/{stopId}/checkin")
    public ResponseEntity<?> checkinStop(@PathVariable Long id, @PathVariable Long stopId,
                                         @RequestBody Map<String, Object> payload) {
        Object emailRaw = payload.get("userEmail");
        Object latRaw = payload.get("lat");
        Object lngRaw = payload.get("lng");
        Object photoRaw = payload.get("photoId");
        if (!(emailRaw instanceof String userEmail) || userEmail.isBlank()
                || !(latRaw instanceof Number) || !(lngRaw instanceof Number)) {
            return ResponseEntity.badRequest().body(Map.of("error", "userEmail, lat and lng are required"));
        }
        double lat = ((Number) latRaw).doubleValue();
        double lng = ((Number) lngRaw).doubleValue();

        Optional<User> userOpt = userRepository.findByEmail(userEmail);
        if (userOpt.isEmpty()) return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        User user = userOpt.get();

        if (!pedipaperRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Pedipaper not found"));
        }

        Optional<RouteParticipation> rpOpt = participationRepository.findByUserIdAndPediId(user.getId(), id);
        if (rpOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Ainda não entraste nesta rota"));
        }
        RouteParticipation rp = rpOpt.get();

        Optional<RouteStop> stopOpt = routeStopRepository.findById(stopId);
        if (stopOpt.isEmpty() || !id.equals(stopOpt.get().getPediId())) {
            return ResponseEntity.badRequest().body(Map.of("error", "Paragem não pertence a esta rota"));
        }
        RouteStop stop = stopOpt.get();

        int totalStops = routeStopRepository.findByPediIdOrderByOrderNumAsc(id).size();

        // Repeating a check-in is idempotent: same counts, no new row.
        if (checkinRepository.findByParticipationIdAndRouteStopId(rp.getParticipationId(), stopId).isPresent()) {
            long done = checkinRepository.countByParticipationId(rp.getParticipationId());
            return ResponseEntity.ok(Map.of("completedStops", (int) done, "totalStops", totalStops));
        }

        Optional<Place> placeOpt = placeRepository.findById(stop.getPlaceId());
        if (placeOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Paragem sem local associado"));
        }
        Place place = placeOpt.get();
        if (place.getLat() == null || place.getLng() == null
                || distanceMeters(lat, lng, place.getLat(), place.getLng()) > MAX_CHECKIN_DISTANCE_METERS) {
            return ResponseEntity.badRequest().body(Map.of("error", "Estás demasiado longe da paragem (máximo 50 m)"));
        }

        Long photoId = null;
        if (Boolean.TRUE.equals(stop.getRequiresPhoto())) {
            if (!(photoRaw instanceof Number)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Esta paragem exige uma foto"));
            }
            photoId = ((Number) photoRaw).longValue();
            Optional<Photo> photoOpt = photoRepository.findById(photoId);
            if (photoOpt.isEmpty() || !user.getId().equals(photoOpt.get().getUserId())
                    || !EVIDENCE.equals(photoOpt.get().getKind())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Foto inválida para esta paragem"));
            }
        }

        RouteStopCheckin checkin = new RouteStopCheckin();
        checkin.setParticipationId(rp.getParticipationId());
        checkin.setRouteStopId(stopId);
        checkin.setLat(lat);
        checkin.setLng(lng);
        checkin.setPhotoId(photoId);
        checkinRepository.save(checkin);

        long done = checkinRepository.countByParticipationId(rp.getParticipationId());
        return ResponseEntity.ok(Map.of("completedStops", (int) done, "totalStops", totalStops));
    }

    // 5. Progress of the current user on a route (used to resume and to show counts).
    @GetMapping("/{id}/progress")
    public ResponseEntity<?> getProgress(@PathVariable Long id, @RequestParam String userEmail) {
        int totalStops = routeStopRepository.findByPediIdOrderByOrderNumAsc(id).size();

        Optional<User> userOpt = userRepository.findByEmail(userEmail);
        if (userOpt.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                    "joined", false, "completed", false,
                    "completedStopIds", List.of(), "totalStops", totalStops, "pointsAwarded", 0));
        }

        Optional<RouteParticipation> rpOpt = participationRepository.findByUserIdAndPediId(userOpt.get().getId(), id);
        if (rpOpt.isEmpty()) {
            return ResponseEntity.ok(Map.of(
                    "joined", false, "completed", false,
                    "completedStopIds", List.of(), "totalStops", totalStops, "pointsAwarded", 0));
        }

        RouteParticipation rp = rpOpt.get();
        List<Long> completedStopIds = checkinRepository.findByParticipationId(rp.getParticipationId()).stream()
                .map(RouteStopCheckin::getRouteStopId)
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of(
                "joined", true,
                "completed", Boolean.TRUE.equals(rp.getCompleted()),
                "completedStopIds", completedStopIds,
                "totalStops", totalStops,
                "pointsAwarded", rp.getPointsAwarded() == null ? 0 : rp.getPointsAwarded()));
    }

    // 6. Complete a Pedipaper (award points only when every stop was checked in)
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
        Pedipaper pedi = pediOpt.get();

        Optional<RouteParticipation> rpOpt = participationRepository.findByUserIdAndPediId(user.getId(), id);
        if (rpOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Not participating in this route"));
        }
        RouteParticipation rp = rpOpt.get();

        int points = pedi.getTotalPoints() == null ? 0 : pedi.getTotalPoints();

        // Already completed: same values, no double points.
        if (Boolean.TRUE.equals(rp.getCompleted())) {
            return ResponseEntity.ok(Map.of(
                    "pointsAwarded", rp.getPointsAwarded() == null ? 0 : rp.getPointsAwarded(),
                    "newBalance", user.getPoints()));
        }

        // Every stop needs a check-in before the route pays out.
        List<RouteStop> stops = routeStopRepository.findByPediIdOrderByOrderNumAsc(id);
        long done = checkinRepository.countByParticipationId(rp.getParticipationId());
        if (done < stops.size()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Ainda faltam paragens por visitar"));
        }

        rp.setCompleted(true);
        rp.setCompletedAt(LocalDateTime.now());
        rp.setPointsAwarded(points);
        participationRepository.save(rp);

        // Atomic points update plus a ledger entry.
        jdbcTemplate.update("UPDATE users SET points = points + ? WHERE id = ?", points, user.getId());
        jdbcTemplate.update(
                "INSERT INTO points_ledger (user_id, amount, reason) VALUES (?, ?, ?)",
                user.getId(), points, "Pedipaper concluído: " + pedi.getName());

        Integer newBalance = jdbcTemplate.queryForObject(
                "SELECT points FROM users WHERE id = ?", Integer.class, user.getId());

        return ResponseEntity.ok(Map.of(
                "pointsAwarded", points,
                "newBalance", newBalance == null ? 0 : newBalance));
    }

    // Great circle distance in metres (haversine).
    private static double distanceMeters(double lat1, double lng1, double lat2, double lng2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_METERS * c;
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
