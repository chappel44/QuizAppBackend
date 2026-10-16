package com.tasks.organizer.authentication.service;

import org.springframework.http.ResponseEntity;

import com.tasks.organizer.authentication.dto.request.SignUpRequest;
import com.tasks.organizer.authentication.dto.request.SigninRequest;
import com.tasks.organizer.authentication.dto.response.JwtAuthenticationResponse;

public interface AuthenticationService {
    ResponseEntity<JwtAuthenticationResponse> signup(SignUpRequest request);

    JwtAuthenticationResponse signin(SigninRequest request);
}
