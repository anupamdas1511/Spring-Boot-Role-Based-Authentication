package com.anupam.auth.service.impl;

import com.anupam.auth.entities.User;
import com.anupam.auth.repositories.UserRepository;
import com.anupam.auth.service.UserService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void saveUser(User user) {
        Optional<User> existingUser = userRepository.findByUsername(user.getUsername());
        if (existingUser.isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setRoles(List.of("USER"));
            userRepository.save(user);
        }
    }

    @Override
    public void saveAdmin(User user) {
        Optional<User> existingUser = userRepository.findByUsername(user.getUsername());
        if (existingUser.isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setRoles(List.of("USER", "ADMIN"));
            userRepository.save(user);
        }
    }

    @Override
    public void updateUser(User user, String username) {
        Optional<User> existingUser = userRepository.findByUsername(username);
        if (existingUser.isPresent()) {
            existingUser.get().setUsername(user.getUsername());
            existingUser.get().setPassword(passwordEncoder.encode(user.getPassword()));
            existingUser.get().setRoles(List.of("USER"));
            userRepository.save(existingUser.get());
        }
    }

    @Override
    public List<User> getAll() {
        return userRepository.findAll();
    }

    @Override
    public Optional<User> findById(ObjectId id) {
        return userRepository.findById(id);
    }

    @Override
    public void deleteById(ObjectId id) {
        userRepository.deleteById(id);
    }

    @Override
    public void deleteByUsername(String username) {
        userRepository.deleteByUsername(username);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}
