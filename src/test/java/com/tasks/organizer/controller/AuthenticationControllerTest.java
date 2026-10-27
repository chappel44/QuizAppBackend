package com.tasks.organizer.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import com.tasks.organizer.user.UserRepository;
import com.tasks.organizer.user.entity.Role;
import com.tasks.organizer.user.entity.User;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional 
class AuthenticationControllerTest {
  @Autowired
  private MockMvc mockMvc;
  @Autowired UserRepository userRepository;
  @Autowired PasswordEncoder passwordEncoder;
  @Autowired EntityManager entityManager;
  
  User testUser;

  private static final AtomicInteger counter = new AtomicInteger(ThreadLocalRandom.current().nextInt(1 << 23));
  private String uniqueIp() {
    int n = counter.getAndIncrement() & 0xFFFFFF;
    return "10." + (n >> 16 & 255) + "." + (n >> 8 & 255) + "." + (n & 255);
  }

  @BeforeEach
  void setUp() {
    testUser = userRepository.save(User.builder()
      .email("john@example.com")
      .password(passwordEncoder.encode("12"))
      .firstName("John")
      .lastName("Smith")
      .role(Role.SITE_ADMIN)
      .build()
    );

    entityManager.flush();
  }

  @Test
  void testDuplicateEmail() throws Exception {
    String jsonPayload = """
    {
      "firstName": "John",
      "lastName": "Smith",
      "email": "john@example.com",
      "password" : "password"
    }
    """;

    mockMvc.perform(post("/api/auth/signup")
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonPayload))
      .andExpect(status().isBadRequest());
  }

  @Test
  void testShortPassword() throws Exception {
    String jsonPayload = """
    {
      "firstName": "John",
      "lastName": "Smith",
      "email": "john.smith@example.com",
      "password" : "1"
    }
    """;

    mockMvc.perform(post("/api/auth/signup")
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonPayload))
      .andExpect(status().isBadRequest());
  }

  @Test 
  void rateLimitAppliesToCreatePasswordResetToken() throws Exception{
    String ip = uniqueIp();
    for(Integer i = 0; i < 10; i++){
      mockMvc.perform(post("/api/auth/password-reset/request")
        .with(request -> { request.setRemoteAddr(ip); return request; })
        .param("email", testUser.getEmail()))
      .andExpect(status().isOk());
    }

    mockMvc.perform(post("/api/auth/password-reset/request")
        .param("email", testUser.getEmail())
        .with(request -> { request.setRemoteAddr(ip); return request; }))
      .andExpect(status().isTooManyRequests());
    
    //Ensure the rate limit applied to one ip doesn't affect another
    mockMvc.perform(post("/api/auth/password-reset/request")
        .with(request -> { request.setRemoteAddr(uniqueIp()); return request; })
        .param("email", testUser.getEmail()))
      .andExpect(status().isOk());
  }

  @Test 
  void rateLimitAppliesToSignIn() throws Exception{
    String ip = uniqueIp();

    String signinBody = """
      {
        "email": "%s",
        "password": "12"
      }
      """.formatted(testUser.getEmail());

    for(Integer i = 0; i < 10; i++){
      mockMvc.perform(post("/api/auth/signin")
        .with(request -> { request.setRemoteAddr(ip); return request; })
        .contentType(MediaType.APPLICATION_JSON)
        .content(signinBody))
      .andExpect(status().isOk());
    }

    mockMvc.perform(post("/api/auth/signin")
        .with(request -> { request.setRemoteAddr(ip); return request; })
        .contentType(MediaType.APPLICATION_JSON)
        .content(signinBody))
      .andExpect(status().isTooManyRequests());

    mockMvc.perform(post("/api/auth/signin")
        .with(request -> { request.setRemoteAddr(uniqueIp()); return request; })
        .contentType(MediaType.APPLICATION_JSON)
        .content(signinBody))
      .andExpect(status().isOk());
  }

  @Test
  void rateLimitAppliesToSignUp() throws Exception{
    String ip = uniqueIp();

    //Signup allows 5 requests per hour per IP
    for(Integer i = 0; i < 5; i++){
      mockMvc.perform(post("/api/auth/signup")
        .with(request -> { request.setRemoteAddr(ip); return request; })
        .contentType(MediaType.APPLICATION_JSON)
        .content(signupBody()))
      .andExpect(status().isOk());
    }

    mockMvc.perform(post("/api/auth/signup")
        .with(request -> { request.setRemoteAddr(ip); return request; })
        .contentType(MediaType.APPLICATION_JSON)
        .content(signupBody()))
      .andExpect(status().isTooManyRequests());

    mockMvc.perform(post("/api/auth/signup")
        .with(request -> { request.setRemoteAddr(uniqueIp()); return request; })
        .contentType(MediaType.APPLICATION_JSON)
        .content(signupBody()))
      .andExpect(status().isOk());
  }

  //Builds a signup body with a unique email so each request creates a new user
  private String signupBody() {
    return """
      {
        "email": "%s@gmail.com",
        "password": "12",
        "firstName": "User",
        "lastName": "New",
        "role": "USER"
      }
      """.formatted(UUID.randomUUID());
  }
}
