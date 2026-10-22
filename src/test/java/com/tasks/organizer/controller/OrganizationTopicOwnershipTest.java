package com.tasks.organizer.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tasks.organizer.attempt.repository.AttemptQuestionRepository;
import com.tasks.organizer.attempt.repository.AttemptRepository;
import com.tasks.organizer.authentication.dto.request.SigninRequest;
import com.tasks.organizer.authentication.dto.response.JwtAuthenticationResponse;
import com.tasks.organizer.authentication.service.AuthenticationService;
import com.tasks.organizer.organization.OrganizationRepository;
import com.tasks.organizer.organization.entity.Organization;
import com.tasks.organizer.organizationmember.OrganizationMemberRepository;
import com.tasks.organizer.organizationmember.entity.OrgPermissions;
import com.tasks.organizer.organizationmember.entity.OrgRoles;
import com.tasks.organizer.organizationmember.entity.OrganizationMember;
import com.tasks.organizer.question.AnswerRepository;
import com.tasks.organizer.question.QuestionRepository;
import com.tasks.organizer.question.entity.Answer;
import com.tasks.organizer.question.entity.Question;
import com.tasks.organizer.section.SectionRepository;
import com.tasks.organizer.section.entity.Section;
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
public class OrganizationTopicOwnershipTest {
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
  @Autowired OrganizationRepository organizationRepository;
  @Autowired OrganizationMemberRepository organizationMemberRepository;
  @Autowired ObjectMapper objectMapper;

  Answer answer;
  User owner, intruder;
  Section section;
  String ownerToken, intruderToken;
  OrganizationMember orgAdmin;
  Organization organization;
  Topic topic;

  String generateTopicPayload(Section currentSection) {
    return """
      {
        "topic": {
          "name": "Chapter 5 Biology Quiz",
          "description": "Covers cell structure and function",
          "topicType": "QUIZ",
          "visibility": "PRIVATE",
          "dueDate": "2026-12-03T18:30:00",
          "isActive": true,
          "questionPoolSize": 1,
          "sectionId": "%s"
        },
        "questions": [
          {
            "points": 1.0,
            "question": "Which organelle produces most of the cell's ATP?",
            "answer": "Mitochondria",
            "answers": [
              { "answer": "Mitochondria", "isCorrect": true }
            ]
          }
        ]
      }
      """.formatted(currentSection.getId());
  }

  String generateTopicUpdatePayload(Topic detachedTopic) throws Exception {
    // The builder-created topic from setUp never has its questions populated, so reload it
    Topic topic = topicRepository.findById(detachedTopic.getId()).orElseThrow();
    List<Map<String, Object>> questions = new ArrayList<>();

    for (Question q : topic.getQuestions()) {
      List<Map<String, Object>> answers = new ArrayList<>();

      for (Answer a : q.getAnswers()) {
        Map<String, Object> answerMap = new LinkedHashMap<>();
        answerMap.put("id", a.getId());
        answerMap.put("answer", a.getAnswer());
        answerMap.put("isCorrect", a.getIsCorrect());
        answerMap.put("isActive", a.isActive());
        answers.add(answerMap);
      }

      Map<String, Object> questionMap = new LinkedHashMap<>();
      questionMap.put("id", q.getId());
      questionMap.put("points", q.getPoints());
      questionMap.put("question", q.getQuestion());
      questionMap.put("answer", q.getAnswer());
      questionMap.put("imageUrl", q.getImageUrl());
      questionMap.put("isActive", q.getIsActive());
      questionMap.put("answers", answers);
      questions.add(questionMap);
    }

    // Only the fields declared on TopicDTO
    Map<String, Object> topicMap = new LinkedHashMap<>();
    topicMap.put("name", topic.getName());
    topicMap.put("description", topic.getDescription());
    topicMap.put("topicType", topic.getTopicType());
    topicMap.put("visibility", topic.getVisibility());
    topicMap.put("dueDate", topic.getDueDate());
    topicMap.put("isActive", topic.getIsActive());
    topicMap.put("questionPoolSize", topic.getQuestionPoolSize());
    topicMap.put("sectionId", topic.getSection().getId());

    Map<String, Object> body = new LinkedHashMap<>();
    body.put("topic", topicMap);
    body.put("questions", questions);

    return objectMapper.writeValueAsString(body);
  }

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

    organization = Organization.builder()
      .name("good org")
      .description("good org")
      .maxOrgAdmins(1)
      .maxStudents(1)
      .maxTeachers(1)
    .build();

    organization = organizationRepository.save(organization);

    orgAdmin = OrganizationMember.builder()
      .organization(organization)
      .role(OrgRoles.ORG_ADMIN)
      .user(owner)
      .permissions(new OrgPermissions[0])
      .build();

    organizationMemberRepository.save(orgAdmin);

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

    topic = topicRepository.save(Topic.builder()
      .name("Test topic")
      .section(section)
      .description("Test description")
      .topicType(TopicType.RANDOM_QUESTIONS)
      .questionPoolSize(1)
      .organization(organization)
      .user(owner)
      .visibility(Visibility.PRIVATE)
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
    entityManager.persist(organization);
    entityManager.persist(topic);
    entityManager.flush();
    entityManager.clear();
  }

  @Transactional 
  void createOrgMember(OrgPermissions[] orgPermissions, User newMember, Organization org){
    OrganizationMember studentMember = OrganizationMember.builder()
      .user(newMember)
      .role(OrgRoles.TEACHER)
      .permissions(orgPermissions)
      .organization(org)
    .build();

    organization.setOrganizationMembers(List.of(studentMember));
    organizationRepository.saveAndFlush(organization);
  }

  @Test 
  void outsiderCannotAccessPrivateTopic() throws Exception {
    mockMvc.perform(get("/api/authenticated/topics")
      .param("topicId", topic.getId().toString())
      .header("Authorization", "Bearer " + intruderToken))
      .andExpect(status().isNotFound());
  }

  @Test 
  void crossOrganizationAccessTopic() throws Exception {
    Organization badOrg = Organization.builder()
      .name("good org")
      .description("good org")
      .maxOrgAdmins(1)
      .maxStudents(1)
      .maxTeachers(1)
    .build();
    badOrg = organizationRepository.save(badOrg);

    organizationMemberRepository.save(OrganizationMember.builder()
      .user(intruder)
      .role(OrgRoles.TEACHER)
      .organization(badOrg)
      .isActive(true)
      .build());

    mockMvc.perform(get("/api/authenticated/topics")
      .param("topicId", topic.getId().toString())
      .header("Authorization", "Bearer " + intruderToken))
      .andExpect(status().isNotFound());
  }

  @Test 
  void adminWithCorrectAnswers() throws Exception {
    mockMvc.perform(get("/api/authenticated/topics")
      .param("topicId", topic.getId().toString())
      .header("Authorization", "Bearer " + ownerToken))
    .andExpect(status().isOk())
    .andExpect(jsonPath("$.data.questions").isNotEmpty())
    .andExpect(jsonPath("$.data.questions[*].id").exists())
    .andExpect(jsonPath("$.data.questions[*].answers[*].id").exists())
    .andExpect(jsonPath("$.data.questions[*].answers[*].answer").exists())
    .andExpect(jsonPath("$.data.questions[*].answers[*].correct").exists()); //Ensure student in an org never receives correct answer
  }

  @Test 
  void studentWithNoCorrectAnswer() throws Exception {
    OrganizationMember studentMember = OrganizationMember.builder()
      .user(intruder)
      .role(OrgRoles.STUDENT)
      .organization(organization)
    .build();

    organizationMemberRepository.save(studentMember);
    organization.setOrganizationMembers(List.of(studentMember));
    organizationRepository.saveAndFlush(organization);

    mockMvc.perform(get("/api/authenticated/topics")
      .param("topicId", topic.getId().toString())
      .header("Authorization", "Bearer " + intruderToken))
    .andExpect(status().isOk())
    .andExpect(jsonPath("$.data.questions").isNotEmpty())
    .andExpect(jsonPath("$.data.questions[*].id").exists())
    .andExpect(jsonPath("$.data.questions[*].answers[*].id").exists())
    .andExpect(jsonPath("$.data.questions[*].answers[*].answer").exists())
    .andExpect(jsonPath("$.data.questions[*].answers[*].correct").doesNotExist()); //Ensure student in an org never receives correct answer
  }

  @Test 
  void canViewPublicOrgTopic() throws Exception {
    Topic managed = topicRepository.findById(topic.getId()).orElseThrow();
    managed.setVisibility(Visibility.PUBLIC);
    topicRepository.saveAndFlush(managed);

    mockMvc.perform(get("/api/authenticated/topics")
      .param("topicId", topic.getId().toString())
      .header("Authorization", "Bearer " + intruderToken))
      .andExpect(status().isOk());
  }

  @Test
  void crossOrganizationCreateTopic() throws Exception {
    String payload = generateTopicPayload(section);

    // A different organization the intruder legitimately belongs to
    Organization otherOrganization = organizationRepository.save(Organization.builder()
      .name("Other org")
      .maxOrgAdmins(1)
      .maxTeachers(5)
      .maxStudents(30)
      .build());

    organizationMemberRepository.save(OrganizationMember.builder()
      .user(intruder)
      .role(OrgRoles.TEACHER)
      .permissions(new OrgPermissions[] { OrgPermissions.CREATE_TOPIC })
      .organization(otherOrganization)
      .isActive(true)
      .build());

    mockMvc.perform(post("/api/authenticated/topics")
        .header("Authorization", "Bearer " + intruderToken)
        .param("sectionId", section.getId().toString())
        .param("organizationId", organization.getId().toString())
        .contentType(MediaType.APPLICATION_JSON)
        .content(payload))
      .andExpect(status().isNotFound());
  }

  @Test
  void teacherWithoutCreatePermissionDenied() throws Exception {
    String payload = generateTopicPayload(section);

    OrganizationMember studentMember = OrganizationMember.builder()
      .user(intruder)
      .role(OrgRoles.TEACHER)
      .organization(organization)
    .build();

    organization.setOrganizationMembers(List.of(studentMember));
    organizationRepository.saveAndFlush(organization);

    mockMvc.perform(post("/api/authenticated/topics")
        .header("Authorization", "Bearer " + intruderToken)
        .param("sectionId", section.getId().toString())
        .param("organizationId", organization.getId().toString())
        .contentType(MediaType.APPLICATION_JSON)
        .content(payload))
      .andExpect(status().isNotFound());
  }

  @Test
  void teacherWithCreatePermissionAllowed() throws Exception {
    String payload = generateTopicPayload(section);

    OrganizationMember studentMember = OrganizationMember.builder()
      .user(intruder)
      .role(OrgRoles.TEACHER)
      .permissions(new OrgPermissions[] { OrgPermissions.CREATE_TOPIC })
      .organization(organization)
    .build();

    organization.setOrganizationMembers(List.of(studentMember));
    organizationRepository.saveAndFlush(organization);

    mockMvc.perform(post("/api/authenticated/topics")
        .header("Authorization", "Bearer " + intruderToken)
        .param("sectionId", section.getId().toString())
        .param("organizationId", organization.getId().toString())
        .contentType(MediaType.APPLICATION_JSON)
        .content(payload))
      .andExpect(status().isOk());
  }

  @Test
  void inactiveTeacherCannotCreateTopic() throws Exception {
    String payload = generateTopicPayload(section);

    OrganizationMember newMember = organizationMemberRepository.save(OrganizationMember.builder()
        .user(intruder)
        .role(OrgRoles.TEACHER)
        .permissions(new OrgPermissions[] { OrgPermissions.CREATE_TOPIC })
        .organization(organization)
        .isActive(false)
      .build()
    );

    organization.setOrganizationMembers(List.of(newMember));
    organizationRepository.saveAndFlush(organization);

    mockMvc.perform(post("/api/authenticated/topics")
        .header("Authorization", "Bearer " + intruderToken)
        .param("sectionId", section.getId().toString())
        .param("organizationId", newMember.getOrganization().getId().toString())
        .contentType(MediaType.APPLICATION_JSON)
        .content(payload))
      .andExpect(status().isForbidden());
  }

  @Test 
  void outsiderCannotUpdateOrgTopic() throws Exception {
    String payload = generateTopicUpdatePayload(topic);
    
    mockMvc.perform(patch("/api/authenticated/topics")
        .header("Authorization", "Bearer " + intruderToken)
        .param("topicId", topic.getId().toString())
        .contentType(MediaType.APPLICATION_JSON)
        .content(payload))
      .andExpect(status().isNotFound());
  }

  @Test 
  void teacherWithoutUpdatePermissionDenied() throws Exception {
    String payload = generateTopicUpdatePayload(topic);
    
    OrganizationMember studentMember = OrganizationMember.builder()
        .user(intruder)
        .role(OrgRoles.TEACHER)
        .organization(organization)
      .build();

    organization.setOrganizationMembers(List.of(studentMember));
    organizationRepository.saveAndFlush(organization);

    mockMvc.perform(patch("/api/authenticated/topics")
        .header("Authorization", "Bearer " + intruderToken)
        .param("topicId", topic.getId().toString())
        .contentType(MediaType.APPLICATION_JSON)
        .content(payload))
      .andExpect(status().isNotFound());
  }

  @Test
  void teacherWithUpdatePermissionAllowed() throws Exception {
    String payload = generateTopicPayload(section);

    OrganizationMember studentMember = OrganizationMember.builder()
      .user(intruder)
      .role(OrgRoles.TEACHER)
      .permissions(new OrgPermissions[] { OrgPermissions.UPDATE_TOPIC })
      .organization(organization)
    .build();

    organization.setOrganizationMembers(List.of(studentMember));
    organizationRepository.saveAndFlush(organization);

    mockMvc.perform(patch("/api/authenticated/topics")
        .header("Authorization", "Bearer " + intruderToken)
        .param("topicId", topic.getId().toString())
        .contentType(MediaType.APPLICATION_JSON)
        .content(payload))
      .andExpect(status().isNoContent());
  }

  @Test
  void inactiveTeacherCannotUpdateTopic() throws Exception {
    String payload = generateTopicPayload(section);

    OrganizationMember newMember = organizationMemberRepository.save(OrganizationMember.builder()
        .user(intruder)
        .role(OrgRoles.TEACHER)
        .permissions(new OrgPermissions[] { OrgPermissions.UPDATE_TOPIC })
        .organization(organization)
        .isActive(false)
      .build()
    );

    organization.setOrganizationMembers(List.of(newMember));
    organizationRepository.saveAndFlush(organization);

    mockMvc.perform(patch("/api/authenticated/topics")
        .header("Authorization", "Bearer " + intruderToken)
        .param("topicId", topic.getId().toString())
        .contentType(MediaType.APPLICATION_JSON)
        .content(payload))
      .andExpect(status().isForbidden());
  }

  @Test
  void outsiderCannotDeleteTopic() throws Exception {
    mockMvc.perform(delete("/api/authenticated/topics")
        .header("Authorization", "Bearer " + intruderToken)
        .param("topicId", topic.getId().toString()))
      .andExpect(status().isNotFound());
    
    assertTrue(topicRepository.findById(topic.getId()).isPresent());
  }

  @Test
  void teacherWithoutDeletePermissionsDenied() throws Exception {
    organizationMemberRepository.save(OrganizationMember.builder()
      .user(intruder)
      .role(OrgRoles.TEACHER)
      .permissions(new OrgPermissions[] { OrgPermissions.CREATE_TOPIC, OrgPermissions.UPDATE_TOPIC }) // has some permission, but not DELETE_TOPIC
      .organization(organization)
      .isActive(true)
      .build());
    
      mockMvc.perform(delete("/api/authenticated/topics")
        .header("Authorization", "Bearer " + intruderToken)
        .param("topicId", topic.getId().toString()))
      .andExpect(status().isNotFound());

    // Deny means nothing changed
    assertTrue(topicRepository.findById(topic.getId()).isPresent());
  }

  @Test
  void teacherWithDeletePermissionsAllowed() throws Exception {
    organizationMemberRepository.save(OrganizationMember.builder()
      .user(intruder)
      .role(OrgRoles.TEACHER)
      .permissions(new OrgPermissions[] { OrgPermissions.CREATE_TOPIC, OrgPermissions.DELETE_TOPIC, OrgPermissions.UPDATE_TOPIC }) // has some permission, but not DELETE_TOPIC
      .organization(organization)
      .isActive(true)
      .build());
    
      mockMvc.perform(delete("/api/authenticated/topics")
        .header("Authorization", "Bearer " + intruderToken)
        .param("topicId", topic.getId().toString()))
      .andExpect(status().isNoContent());

    // Deny means nothing changed
    assertTrue(topicRepository.findById(topic.getId()).isEmpty());
  }
}
