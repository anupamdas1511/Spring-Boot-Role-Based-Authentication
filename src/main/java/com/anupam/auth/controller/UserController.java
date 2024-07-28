package com.anupam.auth.controller;

import com.anupam.auth.entities.Profile;
import com.anupam.auth.entities.User;
import com.anupam.auth.service.ArticleService;
import com.anupam.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final ArticleService articleService;


    @PutMapping
    public ResponseEntity<?> updateUser(@RequestBody User user) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User userInDB = userService.findByUsername(username).orElse(null);
        assert userInDB != null;
        userInDB.setUsername(user.getUsername());
        userInDB.setPassword(user.getPassword());
        userService.updateUser(user, username);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/profile")
    public ResponseEntity<?> profile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Profile profile = new Profile();
        profile.setUsername(username);
        profile.setArticles(articleService.searchArticlesByUsername(username));
        return new ResponseEntity<>(profile, HttpStatus.OK);
    }

    @DeleteMapping
    public ResponseEntity<?> deleteUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        userService.deleteByUsername(authentication.getName());
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
