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
import org.springframework.context.annotation.Import;
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
import com.tasks.organizer.organization.entity.Organization;
import com.tasks.organizer.organizationmember.entity.OrgPermissions;
import com.tasks.organizer.organizationmember.entity.OrgRoles;
import com.tasks.organizer.organizationmember.entity.OrganizationMember;
import com.tasks.organizer.question.AnswerRepository;
import com.tasks.organizer.question.QuestionRepository;
import com.tasks.organizer.question.entity.Answer;
import com.tasks.organizer.question.entity.Question;
import com.tasks.organizer.section.SectionRepository;
import com.tasks.organizer.section.entity.Section;
import com.tasks.organizer.support.OrgHelper;
import com.tasks.organizer.topic.TopicRepository;
import com.tasks.organizer.topic.entity.Topic;
import com.tasks.organizer.topic.entity.Visibility;
import com.tasks.organizer.topic.entity.Topic.TopicType;
import com.tasks.organizer.user.UserRepository;
import com.tasks.organizer.user.entity.Role;
import com.tasks.organizer.user.entity.User;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@SpringBootTest 
@AutoConfigureMockMvc 
@Transactional
@Import(OrgHelper.class)
public class AttemptOwnershipTest {
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
  @Autowired OrgHelper orgHelper;

  Answer answer;
  User owner, intruder;
  Attempt attempt;
  Section section;
  AttemptQuestion attemptQuestion;
  String ownerToken, intruderToken;
  Organization ownerOrg;

  //Create owner and intruder objects
  //Creates a Section -> topic -> question -> answer
  //attempt belongs to owner
  //Generates two auth tokens for intruder and owner
  @BeforeEach
  void setUp() {
    owner = userRepository.save(User.builder()
      .email("owner@test.com")
      .password(passwordEncoder.encode("12"))
      .firstName("owner")
      .lastName("owner")
      .role(Role.USER)
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

    ownerOrg = orgHelper.createOrganization("Owners org");
    orgHelper.addMember(ownerOrg, owner, OrgRoles.STUDENT, true, null);

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
      .organization(ownerOrg)
      .user(owner)
      .visibility(Visibility.PRIVATE)
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

    attempt.setAttemptQuestions(List.of(attemptQuestion));

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
  void crossOrgStudentAttemptAccess() throws Exception {
    OrganizationMember intruderMember = orgHelper.createOrgWithMember(intruder, OrgRoles.STUDENT, null);
    String intruderOrgToken = orgHelper.getOrgHeader(intruderToken, intruderMember.getOrganization().getId());

    mockMvc.perform(get("/api/authenticated/attempt")
      .param("attemptId", attempt.getId().toString())
      .header("Authorization", "Bearer " + intruderToken)
      .header("Org-Authorization", intruderOrgToken))
    .andExpect(status().isNotFound());
  }

  @Test 
  void ownerCannotAccessAttemptWithoutProperOrgHeader() throws Exception {
    OrganizationMember studentOrgB = orgHelper.createOrgWithMember(owner, OrgRoles.TEACHER, OrgPermissions.READ_ATTEMPT);
    String studentOrgBToken = orgHelper.getOrgHeader(ownerToken, studentOrgB.getOrganization().getId());

    mockMvc.perform(get("/api/authenticated/attempt")
      .param("attemptId", attempt.getId().toString())
      .header("Authorization", "Bearer " + ownerToken)
      .header("Org-Authorization", studentOrgBToken))
    .andExpect(status().isNotFound());
  }

  @Test 
  void crossOrgAdminAttemptAccess() throws Exception {
    OrganizationMember intruderMember = orgHelper.createOrgWithMember(intruder, OrgRoles.ORG_ADMIN, null);
    String intruderOrgToken = orgHelper.getOrgHeader(intruderToken, intruderMember.getOrganization().getId());

    mockMvc.perform(get("/api/authenticated/attempt")
      .param("attemptId", attempt.getId().toString())
      .header("Authorization", "Bearer " + intruderToken)
      .header("Org-Authorization", intruderOrgToken))
    .andExpect(status().isNotFound());
  }

  @Test 
  void crossOrgTeacherAttemptAccess() throws Exception {
    OrganizationMember intruderMember = orgHelper.createOrgWithMember(intruder, OrgRoles.TEACHER, OrgPermissions.READ_ATTEMPT);
    String intruderOrgToken = orgHelper.getOrgHeader(intruderToken, intruderMember.getOrganization().getId());

    mockMvc.perform(get("/api/authenticated/attempt")
      .param("attemptId", attempt.getId().toString())
      .header("Authorization", "Bearer " + intruderToken)
      .header("Org-Authorization", intruderOrgToken))
    .andExpect(status().isNotFound());
  }

  @Test
  void ownerCanViewAttempt() throws Exception {
    String ownerOrgToken = orgHelper.getOrgHeader(ownerToken, ownerOrg.getId());

    mockMvc.perform(get("/api/authenticated/attempt")
      .param("attemptId", attempt.getId().toString())
      .header("Authorization", "Bearer " + ownerToken)
      .header("Org-Authorization", ownerOrgToken))
    .andExpect(status().isOk());
  }
  
  @Test
  void ownerWithoutTokenCannotViewAttempt() throws Exception {
    mockMvc.perform(get("/api/authenticated/attempt")
      .param("attemptId", attempt.getId().toString())
      .header("Authorization", "Bearer " + ownerToken))
    .andExpect(status().isNotFound());
  }

  @Test 
  void orgAdminCanViewAttempt() throws Exception {
    OrganizationMember orgAdmin = orgHelper.addMember(ownerOrg, intruder, OrgRoles.ORG_ADMIN, true);
    String orgAdminToken = orgHelper.getOrgHeader(intruderToken, orgAdmin.getOrganization().getId());

    mockMvc.perform(get("/api/authenticated/attempt")
      .param("attemptId", attempt.getId().toString())
      .header("Authorization", "Bearer " + intruderToken)
      .header("Org-Authorization", orgAdminToken))
    .andExpect(status().isOk());
  }

  @Test 
  void teacherInSameOrgCanViewAttempt() throws Exception {
    OrganizationMember orgTeacher = orgHelper.addMember(ownerOrg, intruder, OrgRoles.TEACHER, true, OrgPermissions.READ_ATTEMPT);
    String orgTeacherToken = orgHelper.getOrgHeader(intruderToken, orgTeacher.getOrganization().getId());

    mockMvc.perform(get("/api/authenticated/attempt")
      .param("attemptId", attempt.getId().toString())
      .header("Authorization", "Bearer " + intruderToken)
      .header("Org-Authorization", orgTeacherToken))
    .andExpect(status().isOk());
  }

  @Test 
  void studentInSameOrgCannotViewAnotherStudentsAttempt() throws Exception {
    orgHelper.addMember(ownerOrg, intruder, OrgRoles.STUDENT, true);
    String orgStudentToken = orgHelper.getOrgHeader(intruderToken, ownerOrg.getId());

    mockMvc.perform(get("/api/authenticated/attempt")
      .param("attemptId", attempt.getId().toString())
      .header("Authorization", "Bearer " + intruderToken)
      .header("Org-Authorization", orgStudentToken))
    .andExpect(status().isNotFound());
  }

  @Test 
  void studentInSameOrgWithPermissionEscalatedCannotViewAttempt() throws Exception {
    orgHelper.addMember(ownerOrg, intruder, OrgRoles.STUDENT, true, OrgPermissions.READ_ATTEMPT);
    String orgStudentToken = orgHelper.getOrgHeader(intruderToken, ownerOrg.getId());

    mockMvc.perform(get("/api/authenticated/attempt")
      .param("attemptId", attempt.getId().toString())
      .header("Authorization", "Bearer " + intruderToken)
      .header("Org-Authorization", orgStudentToken))
    .andExpect(status().isNotFound());
  }
}