package com.tasks.organizer.authentication.service;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.tasks.organizer.authentication.dto.request.ResetPasswordRequest;
import com.tasks.organizer.authentication.dto.request.SignUpRequest;
import com.tasks.organizer.authentication.dto.request.SigninRequest;
import com.tasks.organizer.authentication.dto.response.JwtAuthenticationResponse;
import com.tasks.organizer.common.dto.ApiResponse;

public interface AuthenticationService {
    ResponseEntity<JwtAuthenticationResponse> signup(SignUpRequest request);

    JwtAuthenticationResponse signin(SigninRequest request);

    ResponseEntity<ApiResponse<Object>> createOrgToken(UUID organizationId);

    ResponseEntity<ApiResponse<Object>> createPasswordResetToken(String email);
    
    ResponseEntity<ApiResponse<Object>> resetPassword(ResetPasswordRequest password, UUID passwordResetToken);
}
