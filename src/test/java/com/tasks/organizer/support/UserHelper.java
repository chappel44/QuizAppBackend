package com.tasks.organizer.support;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.tasks.organizer.authentication.dto.request.SigninRequest;
import com.tasks.organizer.authentication.service.AuthenticationService;
import com.tasks.organizer.user.UserRepository;
import com.tasks.organizer.user.entity.Role;
import com.tasks.organizer.user.entity.User;

/**
 * Reusable user setup for integration tests.
 * Register it on a test class with {@code @Import(UserHelper.class)}.
 */
@TestComponent
public class UserHelper {
  @Autowired UserRepository userRepository;
  @Autowired PasswordEncoder passwordEncoder;
  @Autowired AuthenticationService authenticationService;

  // The stored password is encoded, so keep the raw one around for signing in later
  private final Map<UUID, String> rawPasswords = new ConcurrentHashMap<>();

  public User createUser() {
    return createUser(Role.USER);
  }

  public User createUser(Role role) {
    String rawPassword = randomString();

    User user = userRepository.save(User.builder()
      .firstName(randomString())
      .lastName(randomString())
      .email(randomString() + "@gmail.com")
      .password(passwordEncoder.encode(rawPassword))
      .role(role)
      .build());

    rawPasswords.put(user.getId(), rawPassword);
    return user;
  }

  // Signs in through AuthenticationService and returns the raw JWT (no "Bearer " prefix)
  public String getToken(User user) {
    String rawPassword = rawPasswords.get(user.getId());
    if (rawPassword == null) {
      throw new IllegalArgumentException("User " + user.getId() + " was not created by UserHelper");
    }

    return authenticationService.signin(SigninRequest.builder()
      .email(user.getEmail())
      .password(rawPassword)
      .build())
      .getToken();
  }

  private String randomString() {
    return UUID.randomUUID().toString().replace("-", "").substring(0, 12);
  }
}
