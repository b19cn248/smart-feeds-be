package com.olh.feeds.api.controller;

import com.olh.feeds.dao.entity.User;
import com.olh.feeds.dto.request.user.UserDTO;
import com.olh.feeds.service.impl.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<User> createUser(@Valid @RequestBody UserDTO userDTO) {
        User createdUser = userService.createUser(userDTO);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }

    @GetMapping("/exists/email")
    public ResponseEntity<Boolean> existsByEmail(@RequestParam String email) {
        boolean exists = userService.existsByEmail(email);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/exists/keycloak")
    public ResponseEntity<Boolean> existsByKeycloakId(@RequestParam String keycloakId) {
        boolean exists = userService.existsByKeycloakId(keycloakId);
        return ResponseEntity.ok(exists);
    }
}
