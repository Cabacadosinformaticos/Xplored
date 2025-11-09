package com.Xplored.Xplored.Controller;

import com.Xplored.Xplored.Model.User;
import com.Xplored.Xplored.Model.UserDao;
import com.Xplored.Xplored.Model.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
public class UserController {

    @Autowired
    private UserDao userDao;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/user/get-all")
    public List<User> getUsers() {
        return userDao.getAllusers();
    }

    @PostMapping("/user/save")
    public User save(@RequestBody User user) {
        return userDao.save(user);
    }

    @GetMapping("/user/login")
    public ResponseEntity<User> loginUser(
            @RequestParam String email,
            @RequestParam String password) {

        Optional<User> user = userRepository.findByEmailAndPasswordHash(email, password);

        return user.map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

}
