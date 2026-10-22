package com.tasks.organizer.authentication.service;

import java.util.Map;
import java.util.UUID;

import org.springframework.security.core.userdetails.UserDetails;

import com.tasks.organizer.organization.context.OrgContext;

public interface JwtService {
    String extractUserName(String token);
    String generateToken(Map<String, Object> extraClaims, UserDetails userDetails);

    //This function compares the jwt tokens username to the user pulled from the database by its username 
    boolean isTokenValid(String token, UserDetails userDetails);
    
    UUID extractUserId(String token);

    OrgContext extractOrgContext(String token);
}
