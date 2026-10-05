package com.tasks.organizer.service.impl;

import com.tasks.organizer.dao.request.SignUpRequest;
import com.tasks.organizer.dao.request.SigninRequest;
import com.tasks.organizer.dao.response.JwtAuthenticationResponse;
import com.tasks.organizer.entities.Role;
import com.tasks.organizer.entities.User;
import com.tasks.organizer.repository.UserRepository;
import com.tasks.organizer.service.AuthenticationService;
import com.tasks.organizer.service.JwtService;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    private Map<String, Object> generateClaims(User user){
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("id", user.getId());
        extraClaims.put("role", user.getRole().toString());
        return extraClaims;
    }

    @Override
    public ResponseEntity<JwtAuthenticationResponse> signup(SignUpRequest request) {
        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
        if(existingUser.isPresent()){
            return ResponseEntity.badRequest().build();
        }

        var newUser = User.builder().firstName(request.getFirstName()).lastName(request.getLastName())
            .email(request.getEmail()).password(passwordEncoder.encode(request.getPassword()))
            .role(Role.STUDENT).build();

        User user = userRepository.save(newUser);
        
        var jwt = jwtService.generateToken(generateClaims(user), user);
        return ResponseEntity.ok(
            JwtAuthenticationResponse.builder()
                .token(jwt)
                .build()
        );

    }

    @Override
    public JwtAuthenticationResponse signin(SigninRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password."));

        var jwt = jwtService.generateToken(generateClaims(user), user);
        return JwtAuthenticationResponse.builder().token(jwt).build();
    }
}
