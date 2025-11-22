package com.Xplored.Xplored.Model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserDao {

    @Autowired
    UserRepository userRepository;

    public User save(User user) {
        return userRepository.save(user);
    }

    public void delete(User user) {
        userRepository.delete(user);
    }

    public List<User> getAllusers() {
        List<User> users = new ArrayList<>();
        Streamable.of(userRepository.findAll())
                .forEach(users::add);
        return users;
    }

    // Helper for login: email + passwordHash
    public Optional<User> findByEmailAndPasswordHash(String email, String passwordHash) {
        return userRepository.findByEmailAndPasswordHash(email, passwordHash);
    }
}
