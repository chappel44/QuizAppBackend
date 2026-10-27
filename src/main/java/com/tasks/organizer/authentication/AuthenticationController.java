package com.tasks.organizer.authentication;

import com.tasks.organizer.authentication.dto.request.ResetPasswordRequest;
import com.tasks.organizer.authentication.dto.request.SignUpRequest;
import com.tasks.organizer.authentication.dto.request.SigninRequest;
import com.tasks.organizer.authentication.dto.response.JwtAuthenticationResponse;
import com.tasks.organizer.authentication.service.AuthenticationService;
import com.tasks.organizer.common.dto.ApiResponse;
import com.tasks.organizer.ratelimit.KeyType;
import com.tasks.organizer.ratelimit.RateLimit;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;

    @RateLimit(capacity = 5, period = 1, unit = ChronoUnit.HOURS)
    @PostMapping("/api/auth/signup")
    public ResponseEntity<JwtAuthenticationResponse> signup(@Valid @RequestBody SignUpRequest request) {
        return authenticationService.signup(request);
    }

    @RateLimit(capacity = 10, period = 15, unit = ChronoUnit.MINUTES)
    @PostMapping("/api/auth/signin")
    public ResponseEntity<JwtAuthenticationResponse> signin(@RequestBody SigninRequest request) {
        return ResponseEntity.ok(authenticationService.signin(request));
    }

    @RateLimit(capacity = 10, period = 15, unit = ChronoUnit.MINUTES, keyType = KeyType.USER)
    @PostMapping("/api/authenticated/organization/{organizationId}/create-org-token")
    public ResponseEntity<ApiResponse<Object>> createOrgToken(@PathVariable UUID organizationId) {
        return authenticationService.createOrgToken(organizationId);
    }

    @RateLimit(capacity = 10, period = 1, unit = ChronoUnit.HOURS)
    @PostMapping("/api/auth/password-reset/request")
    public ResponseEntity<ApiResponse<Object>> createPasswordResetToken(@RequestParam (required = true) String email) {
        return authenticationService.createPasswordResetToken(email);
    }

    @RateLimit(capacity = 10, period = 1, unit = ChronoUnit.HOURS)
    @PatchMapping ("/api/auth/password-reset/confirm")
    public ResponseEntity<ApiResponse<Object>> resetPassword(
        @RequestBody ResetPasswordRequest resetPasswordRequest, 
        @RequestParam (required = true) UUID token
    ) {
        return authenticationService.resetPassword(resetPasswordRequest, token);
    }
}
