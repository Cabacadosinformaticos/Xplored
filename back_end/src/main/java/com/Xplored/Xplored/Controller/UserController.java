package com.Xplored.Xplored.Controller;

import com.Xplored.Xplored.Model.LoginRequest;
import com.Xplored.Xplored.Model.RegisterRequest;
import com.Xplored.Xplored.Model.User;
import com.Xplored.Xplored.Model.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/user")
@CrossOrigin(origins = "*")
public class UserController {

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        if (isBlank(req.name) || isBlank(req.email) || isBlank(req.password)) {
            return ResponseEntity.badRequest().body(Map.of("error", "name, email and password are required"));
        }
        if (userRepository.findByEmail(req.email).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Email already registered"));
        }

        User user = new User();
        user.setName(req.name);
        user.setEmail(req.email);
        user.setPasswordHash(passwordEncoder.encode(req.password));
        user.setRole("USER");
        user.setPoints(0);

        User saved = userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PostMapping("/login")
    public ResponseEntity<User> login(@RequestBody LoginRequest req) {
        if (isBlank(req.email) || isBlank(req.password)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Optional<User> userOpt = userRepository.findByEmail(req.email);
        if (userOpt.isPresent() && passwordEncoder.matches(req.password, userOpt.get().getPasswordHash())) {
            return ResponseEntity.ok(userOpt.get());
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @GetMapping("/by-email")
    public ResponseEntity<User> getUserByEmail(@RequestParam String email) {
        return userRepository.findByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/update-profile")
    public ResponseEntity<User> updateProfile(
            @RequestParam String email,
            @RequestParam String name,
            @RequestParam String about,
            @RequestParam String country
    ) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setName(name);
            user.setAbout(about);
            user.setCountry(country);
            User updated = userRepository.save(user);
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
