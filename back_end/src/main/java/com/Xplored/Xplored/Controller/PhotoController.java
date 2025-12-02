package com.Xplored.Xplored.Controller;

import com.Xplored.Xplored.Config.UrlUtil;
import com.Xplored.Xplored.Model.Photo.Photo;
import com.Xplored.Xplored.Model.Photo.PhotoRepository;
import com.Xplored.Xplored.Model.Place.PlaceRepository;
import com.Xplored.Xplored.Model.Review.ReviewRepository;
import com.Xplored.Xplored.Model.User;
import com.Xplored.Xplored.Model.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/photos")
@CrossOrigin(origins = "*")
public class PhotoController {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");
    private static final String GALLERY = "GALLERY";
    private static final String EVIDENCE = "EVIDENCE";

    @Autowired
    private PhotoRepository photoRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlaceRepository placeRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    // Creates a folder named "uploads" in the project root
    private final Path fileStorageLocation = Paths.get("uploads").toAbsolutePath().normalize();

    public PhotoController() {
        try {
            Files.createDirectories(fileStorageLocation);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadPhoto(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") String userIdOrEmail,
            @RequestParam(value = "placeId", required = false) String placeId,
            @RequestParam(value = "reviewId", required = false) String reviewId
    ) {
        // 1. Check the file before touching the disk.
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "File is empty"));
        }
        String extension = extensionOf(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Only jpg, jpeg, png and webp are allowed"));
        }

        // 2. The user must exist.
        Optional<User> userOpt = findUser(userIdOrEmail);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        }
        User user = userOpt.get();

        // 3. Resolve the optional relations before writing the file.
        boolean isAvatar = reviewId != null && reviewId.startsWith("AVATAR");
        boolean isEvidence = reviewId != null && reviewId.startsWith("STOP-EVIDENCE-");
        Long placeIdValue = parseId(placeId);
        Long reviewIdValue = isAvatar || isEvidence ? null : parseId(reviewId);

        if (placeId != null && !placeId.isBlank()) {
            if (placeIdValue == null || !placeRepository.existsById(placeIdValue)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Place not found"));
            }
        }
        if (reviewIdValue != null && !reviewRepository.existsById(reviewIdValue)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Review not found"));
        }

        // 4. Store the file under uploads/ with a generated name.
        String fileName = UUID.randomUUID() + "." + extension;
        Path targetLocation = fileStorageLocation.resolve(fileName).normalize();
        if (!targetLocation.startsWith(fileStorageLocation)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid file path"));
        }
        try {
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Could not store file"));
        }

        String relativeUrl = "/uploads/" + fileName;

        // 5a. Avatar: update the user, no Photo row.
        if (isAvatar) {
            user.setProfilePhotoUrl(relativeUrl);
            userRepository.save(user);
            return ResponseEntity.ok(Map.of("status", "Avatar Updated", "url", UrlUtil.toAbsolute(relativeUrl)));
        }

        // 5b. Regular photo: place gallery, review photo or pedipaper evidence.
        Photo photo = new Photo();
        photo.setUrl(relativeUrl);
        photo.setUserId(user.getId());
        photo.setStatus("APPROVED");
        photo.setKind(isEvidence ? EVIDENCE : GALLERY);
        photo.setPlaceId(placeIdValue);
        photo.setReviewId(reviewIdValue);
        photoRepository.save(photo);

        return ResponseEntity.ok(Map.of("status", "Photo Saved", "url", UrlUtil.toAbsolute(relativeUrl)));
    }

    @GetMapping("/by-place/{placeId}")
    public List<Photo> getByPlace(@PathVariable Long placeId) {
        return photoRepository.findByPlaceIdAndKind(placeId, GALLERY).stream()
                .map(PhotoController::withAbsoluteUrl)
                .collect(Collectors.toList());
    }

    // Fetch photos for the User Profile Gallery.
    @GetMapping("/by-user/{userIdOrEmail}")
    public List<Photo> getByUser(@PathVariable String userIdOrEmail) {
        Optional<User> userOpt = findUser(userIdOrEmail);
        if (userOpt.isEmpty()) return List.of();
        return photoRepository.findByUserIdAndKind(userOpt.get().getId(), GALLERY).stream()
                .map(PhotoController::withAbsoluteUrl)
                .collect(Collectors.toList());
    }

    // Detached copy so the absolute URL never gets flushed back to the database.
    private static Photo withAbsoluteUrl(Photo source) {
        Photo copy = new Photo();
        copy.setPhotoId(source.getPhotoId());
        copy.setUrl(UrlUtil.toAbsolute(source.getUrl()));
        copy.setPlaceId(source.getPlaceId());
        copy.setUserId(source.getUserId());
        copy.setReviewId(source.getReviewId());
        copy.setKind(source.getKind());
        copy.setStatus(source.getStatus());
        return copy;
    }

    private Optional<User> findUser(String idOrEmail) {
        if (idOrEmail == null || idOrEmail.isBlank()) return Optional.empty();
        Optional<User> byEmail = userRepository.findByEmail(idOrEmail);
        if (byEmail.isPresent()) return byEmail;
        try {
            return userRepository.findById(Long.parseLong(idOrEmail));
        } catch (NumberFormatException ex) {
            return Optional.empty();
        }
    }

    private static Long parseId(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static String extensionOf(String originalName) {
        if (originalName == null) return "";
        int dot = originalName.lastIndexOf('.');
        if (dot < 0 || dot == originalName.length() - 1) return "";
        return originalName.substring(dot + 1).toLowerCase();
    }
}
