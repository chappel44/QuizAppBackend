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
}
