package com.Xplored.Xplored.Controller;

import com.Xplored.Xplored.Model.User;
import com.Xplored.Xplored.Model.UserDao;
import com.Xplored.Xplored.Model.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PutMapping;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/user")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserDao userDao;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/get-all")
    public List<User> getUsers() {
        return userDao.getAllusers();
    }

    @PostMapping("/save")
    public ResponseEntity<User> save(@RequestBody User user) {
        User saved = userDao.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/login")
    public ResponseEntity<User> loginUser(
            @RequestParam String email,
            @RequestParam String password) {

        Optional<User> user = userRepository.findByEmailAndPasswordHash(email, password);

        return user.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    // --- NEW ENDPOINT ---
    @GetMapping("/by-email")
    public ResponseEntity<User> getUserByEmail(@RequestParam String email) {
        return userRepository.findByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/update-points")
    public ResponseEntity<?> updatePoints(@RequestParam String email, @RequestParam int points) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setPoints(points);
            userRepository.save(user);
            return ResponseEntity.ok("Points updated");
        }
        return ResponseEntity.notFound().build();
    }
    @PutMapping("/update-profile")
    public ResponseEntity<User> updateProfile(
            @RequestParam String email,
            @RequestParam String name,
            @RequestParam String about,
            @RequestParam String country // <--- NEW PARAMETER
    ) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            user.setName(name);
            user.setAbout(about);
            user.setCountry(country); // <--- SAVE IT
            User updated = userRepository.save(user);
            return ResponseEntity.ok(updated);
        }
        return ResponseEntity.notFound().build();
    }
}