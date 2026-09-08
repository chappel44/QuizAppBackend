package com.tasks.organizer.service;

import java.util.Map;
import java.util.UUID;

import org.springframework.security.core.userdetails.UserDetails;

public interface JwtService {
    String extractUserName(String token);
    String generateToken(Map<String, Object> extraClaims, UserDetails userDetails);
    boolean isTokenValid(String token, UserDetails userDetails);
    UUID extractUserId(String token);
}
