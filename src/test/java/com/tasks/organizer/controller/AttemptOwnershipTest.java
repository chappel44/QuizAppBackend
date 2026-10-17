package com.tasks.organizer.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import com.tasks.organizer.attempt.entity.Attempt;
import com.tasks.organizer.attempt.entity.AttemptQuestion;
import com.tasks.organizer.attempt.repository.AttemptRepository;
import com.tasks.organizer.authentication.dto.request.SigninRequest;
import com.tasks.organizer.authentication.dto.response.JwtAuthenticationResponse;
import com.tasks.organizer.authentication.service.AuthenticationService;
import com.tasks.organizer.question.QuestionRepository;
import com.tasks.organizer.question.entity.Answer;
import com.tasks.organizer.question.entity.Question;
import com.tasks.organizer.section.SectionRepository;
import com.tasks.organizer.section.entity.Section;
import com.tasks.organizer.topic.TopicRepository;
import com.tasks.organizer.topic.entity.Topic;
import com.tasks.organizer.topic.entity.Topic.TopicType;
import com.tasks.organizer.user.UserRepository;
import com.tasks.organizer.user.entity.Role;
import com.tasks.organizer.user.entity.User;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@SpringBootTest 
@AutoConfigureMockMvc 
@Transactional
class AttemptOwnershipTest {
  @Autowired MockMvc mockMvc;
  @Autowired UserRepository userRepository;
  @Autowired AttemptRepository attemptRepository;
  @Autowired TopicRepository topicRepository;
  @Autowired QuestionRepository questionRepository;
  @Autowired SectionRepository sectionRepository;
  @Autowired PasswordEncoder passwordEncoder;
  @Autowired AuthenticationService authenticationService;
  @Autowired EntityManager entityManager;

  User owner, intruder;
  Attempt attempt;

  @BeforeEach
  void setUp() {
    owner = userRepository.save(User.builder()
      .email("owner@test.com")
      .password(passwordEncoder.encode("12"))
      .firstName("owner")
      .lastName("owner")
      .role(Role.STUDENT)
      .build()
    );

    intruder = userRepository.save(User.builder()
      .email("intruder@test.com")
      .password(passwordEncoder.encode("12"))
      .firstName("intruder")
      .lastName("intruder")
      .role(Role.STUDENT)
      .build()
    );

    Section section = sectionRepository.save(Section.builder()
      .name("test section description")
      .description("test section description")
      .build()
    );

    Topic topic = topicRepository.save(Topic.builder()
      .name("Test topic")
      .section(section)
      .description("Test description")
      .topicType(TopicType.RANDOM_QUESTIONS)
      .questionPoolSize(1)
    .build());

    Question question = questionRepository.save(Question.builder()
      .question("test question")
      .points(1)
      .topic(topic)
      .points(1)
    .build());

    question.setAnswers(List.of(Answer.builder()
      .answer("TEst answer")
      .isCorrect(true)
      .question(question)
    .build()));

    attempt  = attemptRepository.save(
      Attempt.builder()
        .user(owner)
        .topic(topic)
        .pointsEarned(0)
        .totalPoints(1)
        .percentage(0)
      .build()
    );

    attempt.setAttemptQuestions(List.of(AttemptQuestion.builder()
        .attempt(attempt)
        .question(question)
      .build()
    ));

    entityManager.flush();
    entityManager.clear();
  }

  @Test 
  void otherUserCannotViewAttempt() throws Exception {
    SigninRequest signinRequest = SigninRequest.builder()
    .email(intruder.getEmail())
    .password("12")
    .build();

    JwtAuthenticationResponse intruderRes = authenticationService.signin(signinRequest);

    mockMvc.perform(get("/api/student/attempt")
      .param("attemptId", attempt.getId().toString())
      .header("Authorization", "Bearer " + intruderRes.getToken()))
    .andExpect(status().isNotFound());
  }

  @Test
  void ownerCanViewAttempt() throws Exception {
    SigninRequest signinRequest = SigninRequest.builder()
      .email(owner.getEmail())
      .password("12")
    .build();

    JwtAuthenticationResponse ownerRes = authenticationService.signin(signinRequest);

    mockMvc.perform(get("/api/student/attempt")
      .param("attemptId", attempt.getId().toString())
      .header("Authorization", "Bearer " + ownerRes.getToken()))
    .andExpect(status().isOk());
  }
}