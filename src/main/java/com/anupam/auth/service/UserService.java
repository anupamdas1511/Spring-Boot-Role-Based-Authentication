package com.anupam.auth.service;

import com.anupam.auth.entities.User;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Optional;

public interface UserService {
    void saveUser(User user);
    void updateUser(User user, String username);
    List<User> getAll();
    Optional<User> findById(ObjectId id);
    void deleteById(ObjectId id);
    void deleteByUsername(String username);
    Optional<User> findByUsername(String username);
}
