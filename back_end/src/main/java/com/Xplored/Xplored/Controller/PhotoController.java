package com.Xplored.Xplored.Controller;

import com.Xplored.Xplored.Model.Photo.Photo;
import com.Xplored.Xplored.Model.Photo.PhotoRepository;
import com.Xplored.Xplored.Model.Place.Place;
import com.Xplored.Xplored.Model.Place.PlaceRepository;
import com.Xplored.Xplored.Model.User;
import com.Xplored.Xplored.Model.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.Optional;

@RestController
@RequestMapping("/photos")
@CrossOrigin(origins = "*")
public class PhotoController {

    @Autowired
    private PhotoRepository photoRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PlaceRepository placeRepository;

    // Creates a folder named "uploads" in your project root
    private final Path fileStorageLocation = Paths.get("uploads").toAbsolutePath().normalize();

    public PhotoController() {
        try {
            Files.createDirectories(fileStorageLocation);
        } catch (Exception ex) {
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
        try {
            // 1. Save File to Disk
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path targetLocation = fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // 2. Generate URL (10.0.2.2 is for Android Emulator to reach localhost)
            String fileUrl = "http://10.0.2.2:9000/uploads/" + fileName;

            // 3. Find User (Accepts ID or Email)
            Optional<User> userOpt = userRepository.findByEmail(userIdOrEmail);
            if (userOpt.isEmpty()) {
                try {
                    Long uid = Long.parseLong(userIdOrEmail);
                    userOpt = userRepository.findById(uid);
                } catch (NumberFormatException ignored) {}
            }

            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest().body("User not found: " + userIdOrEmail);
            }
            User user = userOpt.get();

            // Case A: Avatar Update (Updates User table, does not create Photo entity)
            if (reviewId != null && reviewId.startsWith("AVATAR")) {
                user.setProfilePhotoUrl(fileUrl);
                userRepository.save(user);
                return ResponseEntity.ok().body("{\"status\": \"Avatar Updated\", \"url\": \"" + fileUrl + "\"}");
            }

            // Case B & C: Gallery or Review/Place Photo
            Photo photo = new Photo();
            photo.setUrl(fileUrl);
            photo.setUserId(user.getId());
            photo.setStatus("APPROVED");

            // Handle Review Link
            if (reviewId != null && !reviewId.isEmpty() && !reviewId.startsWith("PLACE") && !reviewId.startsWith("PROFILE")) {
                try {
                    Long rId = Long.parseLong(reviewId);
                    photo.setReviewId(rId);
                } catch (NumberFormatException e) {
                    photo.setReviewId(null);
                }
            }

            // Handle Place Link (Optional now)
            if (placeId != null && !placeId.isEmpty()) {
                try {
                    Long pId = Long.parseLong(placeId);
                    photo.setPlaceId(pId);

                    // Update Place cover image if missing
                    Optional<Place> placeOpt = placeRepository.findById(pId);
                    if (placeOpt.isPresent()) {
                        Place place = placeOpt.get();
                        if (place.getCoverImageUrl() == null || place.getCoverImageUrl().isEmpty()) {
                            place.setCoverImageUrl(fileUrl);
                            placeRepository.save(place);
                        }
                    }
                } catch (NumberFormatException e) {
                    // Invalid place ID, but we still save the photo as a user gallery photo
                }
            }

            // SAVE TO DB
            photoRepository.save(photo);

            return ResponseEntity.ok().body("{\"status\": \"Photo Saved\", \"url\": \"" + fileUrl + "\"}");

        } catch (IOException ex) {
            return ResponseEntity.badRequest().body("Error: " + ex.getMessage());
        }
    }

    @GetMapping("/by-place/{placeId}")
    public List<Photo> getByPlace(@PathVariable Long placeId) {
        return photoRepository.findByPlaceId(placeId);
    }

    // NEW: Endpoint to fetch photos for the User Profile Gallery
    @GetMapping("/by-user/{userIdOrEmail}")
    public List<Photo> getByUser(@PathVariable String userIdOrEmail) {
        try {
            Long uid = Long.parseLong(userIdOrEmail);
            return photoRepository.findByUserId(uid);
        } catch (NumberFormatException e) {
            Optional<User> u = userRepository.findByEmail(userIdOrEmail);
            if (u.isPresent()) {
                return photoRepository.findByUserId(u.get().getId());
            }
        }
        return List.of();
    }
}