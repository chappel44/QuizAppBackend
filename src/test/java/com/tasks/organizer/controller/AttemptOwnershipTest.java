package com.tasks.organizer.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import com.tasks.organizer.attempt.entity.Attempt;
import com.tasks.organizer.attempt.entity.AttemptQuestion;
import com.tasks.organizer.attempt.repository.AttemptQuestionRepository;
import com.tasks.organizer.attempt.repository.AttemptRepository;
import com.tasks.organizer.authentication.dto.request.SigninRequest;
import com.tasks.organizer.authentication.dto.response.JwtAuthenticationResponse;
import com.tasks.organizer.authentication.service.AuthenticationService;
import com.tasks.organizer.question.AnswerRepository;
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
  @Autowired AnswerRepository answerRepository;
  @Autowired AttemptQuestionRepository attemptQuestionRepository;
  @Autowired PasswordEncoder passwordEncoder;
  @Autowired AuthenticationService authenticationService;
  @Autowired EntityManager entityManager;

  Answer answer;
  User owner, intruder;
  Attempt attempt;
  Section section;
  AttemptQuestion attemptQuestion;
  String ownerToken, intruderToken;

  @BeforeEach
  void setUp() {
    owner = userRepository.save(User.builder()
      .email("owner@test.com")
      .password(passwordEncoder.encode("12"))
      .firstName("owner")
      .lastName("owner")
      .role(Role.SITE_ADMIN)
      .build()
    );

    intruder = userRepository.save(User.builder()
      .email("intruder@test.com")
      .password(passwordEncoder.encode("12"))
      .firstName("intruder")
      .lastName("intruder")
      .role(Role.USER)
      .build()
    );

    section = sectionRepository.save(Section.builder()
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
    .build());

    answer = answerRepository.save(
      Answer.builder()
        .answer("TEst answer")
        .isCorrect(true)
        .question(question)
      .build()
    );

    question.setAnswers(List.of(answer));

    attempt = attemptRepository.save(
      Attempt.builder()
        .user(owner)
        .topic(topic)
        .pointsEarned(0)
        .totalPoints(1)
        .percentage(0)
      .build()
    );

    attemptQuestion = attemptQuestionRepository.save(
      AttemptQuestion.builder()
        .attempt(attempt)
        .question(question)
      .build()
    );

    attempt.setAttemptQuestions(List.of(
    ));

    SigninRequest intruderRequest = SigninRequest.builder()
    .email(intruder.getEmail())
    .password("12")
    .build();
    JwtAuthenticationResponse intruderRes = authenticationService.signin(intruderRequest);
    
    SigninRequest ownerRequest = SigninRequest.builder()
    .email(owner.getEmail())
    .password("12")
    .build();
    JwtAuthenticationResponse ownerRes = authenticationService.signin(ownerRequest);

    intruderToken = intruderRes.getToken();
    ownerToken = ownerRes.getToken();

    entityManager.flush();
    entityManager.clear();
  }

  @Test 
  void otherUserCannotViewAttempt() throws Exception {
    mockMvc.perform(get("/api/student/attempt")
      .param("attemptId", attempt.getId().toString())
      .header("Authorization", "Bearer " + intruderToken))
    .andExpect(status().isNotFound());
  }

  @Test
  void ownerCanViewAttempt() throws Exception {
    mockMvc.perform(get("/api/student/attempt")
      .param("attemptId", attempt.getId().toString())
      .header("Authorization", "Bearer " + ownerToken))
    .andExpect(status().isOk());
  }

  @Test 
  void otherUserCannotSubmitQuestionForAnotherUserAttempt() throws Exception {
    mockMvc.perform(patch("/api/student/attempt/record/question")
      .param("answerId", answer.getId().toString())
      .param("attemptQuestionId", attemptQuestion.getId().toString())
      .header("Authorization", "Bearer " + intruderToken))
    .andExpect(status().isNotFound());
  }

  @Test 
  void userCannotAccessAdminRoute() throws Exception {
    String body = """
      {
        "topic": {
          "name": "Admin only topic",
          "description": "Should not be created by a student",
          "topicType": "QUIZ",
          "dueDate": "2026-12-03T18:30:00",
          "isActive": true,
          "questionPoolSize": 1,
        },
        "questions": [
          {
            "points": 1.0,
            "question": "What is 2 + 2?",
            "answer": "4",
            "answers": [
              { "answer": "4", "isCorrect": true }
            ]
          }
        ]
      }
      """.formatted(section.getId());

    mockMvc.perform(post("/api/admin/topics")
      .param("sectionId", section.getId().toString())
      .contentType(MediaType.APPLICATION_JSON)
      .content(body)
      .header("Authorization", "Bearer " + intruderToken))
    .andExpect(status().isForbidden());
  }
}