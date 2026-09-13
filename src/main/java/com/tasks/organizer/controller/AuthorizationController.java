package com.tasks.organizer.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthorizationController {

    @GetMapping
    public ResponseEntity<String> sayHello() {
        return ResponseEntity.ok("Here is your resource");
    }

    @GetMapping("/student")
    public ResponseEntity<String> user() {
        return ResponseEntity.ok("Here is your resource user");
    }

    @GetMapping("/admin")
    public ResponseEntity<String> admin(Authentication authentication) {
        return ResponseEntity.ok("Here is your resource admin");
    }
}
