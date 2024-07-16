package com.anupam.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @GetMapping("/user")
    public ResponseEntity<String> userResponse() {
        return ResponseEntity.ok("User Response");
    }

    @GetMapping("/admin")
    public ResponseEntity<String> adminResponse() {
        return ResponseEntity.ok("Admin Response");
    }
}
