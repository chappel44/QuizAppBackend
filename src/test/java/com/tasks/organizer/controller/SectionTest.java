package com.tasks.organizer.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.apache.commons.lang3.ObjectUtils.Null;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.tasks.organizer.organizationmember.entity.OrgPermissions;
import com.tasks.organizer.organizationmember.entity.OrgRoles;
import com.tasks.organizer.organizationmember.entity.OrganizationMember;
import com.tasks.organizer.section.SectionRepository;
import com.tasks.organizer.section.entity.Section;
import com.tasks.organizer.support.AttemptHelper;
import com.tasks.organizer.support.OrgHelper;
import com.tasks.organizer.support.TopicHelper;
import com.tasks.organizer.support.UserHelper;
import com.tasks.organizer.user.entity.User;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

@SpringBootTest 
@AutoConfigureMockMvc 
@Slf4j
@Transactional 
@Import ({OrgHelper.class, TopicHelper.class, AttemptHelper.class, UserHelper.class})
@ExtendWith (OutputCaptureExtension.class)
public class SectionTest {
  @Autowired MockMvc mockMvc;
  @Autowired UserHelper userHelper;
  @Autowired TopicHelper topicHelper;
  @Autowired OrgHelper orgHelper;
  @Autowired SectionRepository sectionRepository;

  private static UUID orgId(Section s) {
    return s.getOrganization() == null ? null : s.getOrganization().getId();
  }

  boolean sectionsSame(Section section1, Section section2){
    return section1.getId().equals(section2.getId())
    && section1.getCreatedAt().equals(section2.getCreatedAt())
    && section1.getName().equals(section2.getName())
    && section1.getDescription().equals(section2.getDescription())
    && section1.getIsActive().equals(section2.getIsActive())
    && orgId(section1).equals(orgId(section2)) 
    && section1.getUser().getId().equals(section2.getUser().getId());
  }

  void print(String str){
    System.out.println(str);
  }

  @Test 
  void adminCanUpdateSection() throws Exception {
    String jsonPayload = """
    {
      "name": "John",
      "description": "Smith",
      "isActive": true
    }
    """;

    User user = userHelper.createUser();
    String token = userHelper.getToken(user);

    OrganizationMember organizationMember = orgHelper.createOrgWithMember(user, OrgRoles.ORG_ADMIN, OrgPermissions.UPDATE_TOPIC);

    Section section = topicHelper.createSection("Random section", organizationMember.getOrganization(), user);

    String orgHeader = orgHelper.getOrgHeader(token, organizationMember.getOrganization().getId());

    MvcResult mvcResult = mockMvc.perform(patch("/api/authenticated/section")
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonPayload)
      .header("Authorization", "Bearer " + token)
      .header("Org-Authorization", orgHeader)
      .param("sectionId", section.getId().toString())
    )
    .andExpect(status().isNoContent())
    .andReturn();

    String responseBody = mvcResult.getResponse().getContentAsString();
    log.info("Response Body: {}", responseBody);
  }

  @Test
  void adminCannotUpdateSectionOutsideTheirOrg(CapturedOutput output) throws Exception {
    String jsonPayload = """
    {
      "name": "John",
      "description": "Smith",
      "isActive": true
    }
    """;

    User user = userHelper.createUser();
    String token = userHelper.getToken(user);

    OrganizationMember organizationMember = orgHelper.createOrgWithMember(user, OrgRoles.ORG_ADMIN, OrgPermissions.UPDATE_TOPIC);

    Section section = topicHelper.createSection("Random section", orgHelper.createOrganization("Randos"), user);

    String orgHeader = orgHelper.getOrgHeader(token, organizationMember.getOrganization().getId());

    mockMvc.perform(patch("/api/authenticated/section")
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonPayload)
      .header("Authorization", "Bearer " + token)
      .header("Org-Authorization", orgHeader)
      .param("sectionId", section.getId().toString())
    )
    .andExpect(status().isNotFound())
    .andReturn();

    assertThat(output.getOut().contains("Organization ids don't match:"));
  }

  @Test 
  void TeacherCannotUpdateSectionWithoutPermissions(CapturedOutput output) throws Exception {
    String jsonPayload = """
    {
      "name": "John",
      "description": "Smith",
      "isActive": true
    }
    """;

    User user = userHelper.createUser();
    String token = userHelper.getToken(user);

    OrganizationMember organizationMember = orgHelper.createOrgWithMember(user, OrgRoles.TEACHER, OrgPermissions.READ_ATTEMPT);

    Section section = topicHelper.createSection("Random section", organizationMember.getOrganization(), user);

    String orgHeader = orgHelper.getOrgHeader(token, organizationMember.getOrganization().getId());

    mockMvc.perform(patch("/api/authenticated/section")
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonPayload)
      .header("Authorization", "Bearer " + token)
      .header("Org-Authorization", orgHeader)
      .param("sectionId", section.getId().toString())
    )
    .andExpect(status().isNotFound())
    .andReturn();

    assertThat(output.getOut().contains("Organization ids don't match:"));
  }

  @Test 
  void TeacherCanUpdateSectionWithPermissionsVerifyUpdate(CapturedOutput output) throws Exception {
    String jsonPayload = """
    {
      "name": "Testing section",
      "description": "This is a test",
      "isActive": false
    }
    """;

    User user = userHelper.createUser();
    String token = userHelper.getToken(user);

    OrganizationMember organizationMember = orgHelper.createOrgWithMember(user, OrgRoles.TEACHER, OrgPermissions.UPDATE_TOPIC);

    Section section = topicHelper.createSection("Random section", organizationMember.getOrganization(), user);

    String ogName = section.getName();
    String ogDescription = section.getDescription();
    Boolean ogActive = section.getIsActive();

    String orgHeader = orgHelper.getOrgHeader(token, organizationMember.getOrganization().getId());

    mockMvc.perform(patch("/api/authenticated/section")
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonPayload)
      .header("Authorization", "Bearer " + token)
      .header("Org-Authorization", orgHeader)
      .param("sectionId", section.getId().toString())
    )
    .andExpect(status().isNoContent())
    .andReturn();

    Section sectionFromDb = sectionRepository.findById(section.getId()).orElse(null);

    boolean isSame = !ogName.equals(sectionFromDb.getName()) && 
      !ogDescription.equals(sectionFromDb.getDescription()) &&
      !ogActive.equals(sectionFromDb.getIsActive());

    assertTrue(isSame);
  }

  @Test
  void studentCannotUpdateOrgSectionsInSameOrg(CapturedOutput output) throws Exception {
    String jsonPayload = """
    {
      "name": "John",
      "description": "Smith",
      "isActive": true
    }
    """;

    User user = userHelper.createUser();
    User adminUser = userHelper.createUser();
    String token = userHelper.getToken(user);

    OrganizationMember organizationMember = orgHelper.createOrgWithMember(user, OrgRoles.STUDENT, OrgPermissions.UPDATE_TOPIC);
    orgHelper.addMember(organizationMember.getOrganization(), adminUser, OrgRoles.TEACHER, true, OrgPermissions.CREATE_TOPIC);

    Section section = topicHelper.createSection("Random section", organizationMember.getOrganization(), adminUser);

    String orgHeader = orgHelper.getOrgHeader(token, organizationMember.getOrganization().getId());

    mockMvc.perform(patch("/api/authenticated/section")
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonPayload)
      .header("Authorization", "Bearer " + token)
      .header("Org-Authorization", orgHeader)
      .param("sectionId", section.getId().toString())
    )
    .andExpect(status().isNotFound())
    .andReturn();

    Section sectionFromDb = sectionRepository.findById(section.getId()).orElse(null);
    assertThat(sectionFromDb)
    .usingRecursiveComparison()
    .comparingOnlyFields("id", "name", "description", "isActive")
    .isEqualTo(section);
  }

  @Test
  void publicUserCanUpdateTheirSection(CapturedOutput output) throws Exception {
    String jsonPayload = """
    {
      "name": "John",
      "description": "Smith",
      "isActive": true
    }
    """;

    User user = userHelper.createUser();
    String token = userHelper.getToken(user);

    Section section = topicHelper.createSection("Random section", null, user);

    mockMvc.perform(patch("/api/authenticated/section")
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonPayload)
      .header("Authorization", "Bearer " + token)
      .param("sectionId", section.getId().toString())
    )
    .andExpect(status().isNoContent());

    Section sectionFromDb = sectionRepository.findById(section.getId()).orElse(null);
    assertThat(sectionFromDb)
    .usingRecursiveComparison()
    .comparingOnlyFields("id", "name", "description", "isActive")
    .isEqualTo(section);
  }

  @Test
  void publicUserCannotUpdateSectionTheyDontOwn(CapturedOutput output) throws Exception {
    String jsonPayload = """
    {
      "name": "John",
      "description": "Smith",
      "isActive": true
    }
    """;

    User user = userHelper.createUser();
    String token = userHelper.getToken(user);

    User ownerUser = userHelper.createUser();
    Section section = topicHelper.createSection("Random section", null, ownerUser);

    mockMvc.perform(patch("/api/authenticated/section")
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonPayload)
      .header("Authorization", "Bearer " + token)
      .param("sectionId", section.getId().toString())
    )
    .andExpect(status().isNotFound());

    Section sectionFromDb = sectionRepository.findById(section.getId()).orElse(null);
    assertThat(sectionFromDb)
    .usingRecursiveComparison()
    .comparingOnlyFields("id", "name", "description", "isActive")
    .isEqualTo(section);
  }

  @Test 
  void crossOrgAdminCannotUpdateSection() throws Exception {
    String jsonPayload = """
    {
      "name": "John",
      "description": "Smith",
      "isActive": true
    }
    """;

    User user = userHelper.createUser();

    OrganizationMember organizationMember = orgHelper.createOrgWithMember(user, OrgRoles.ORG_ADMIN, OrgPermissions.UPDATE_TOPIC);
    Section section = topicHelper.createSection("Random section", organizationMember.getOrganization(), user);

    User intruderUser = userHelper.createUser();
    String intruderToken = userHelper.getToken(intruderUser);
    OrganizationMember intruderAdmin = orgHelper.createOrgWithMember(intruderUser, OrgRoles.ORG_ADMIN, null);
    String intruderOrgHeader = orgHelper.getOrgHeader(intruderToken, intruderAdmin.getOrganization().getId());

    MvcResult mvcResult = mockMvc.perform(patch("/api/authenticated/section")
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonPayload)
      .header("Authorization", "Bearer " + intruderToken)
      .header("Org-Authorization", intruderOrgHeader)
      .param("sectionId", section.getId().toString())
    )
    .andExpect(status().isNotFound())
    .andReturn();

    String responseBody = mvcResult.getResponse().getContentAsString();
    log.info("Response Body: {}", responseBody);
  }

  @Test 
  void crossOrgTeacherCannotUpdateSection() throws Exception {
    String jsonPayload = """
    {
      "name": "John",
      "description": "Smith",
      "isActive": true
    }
    """;

    User user = userHelper.createUser();

    OrganizationMember organizationMember = orgHelper.createOrgWithMember(user, OrgRoles.ORG_ADMIN, OrgPermissions.UPDATE_TOPIC);
    Section section = topicHelper.createSection("Random section", organizationMember.getOrganization(), user);

    User intruderUser = userHelper.createUser();
    String intruderToken = userHelper.getToken(intruderUser);
    OrganizationMember intruderAdmin = orgHelper.createOrgWithMember(intruderUser, OrgRoles.TEACHER, OrgPermissions.UPDATE_TOPIC);
    String intruderOrgHeader = orgHelper.getOrgHeader(intruderToken, intruderAdmin.getOrganization().getId());

    MvcResult mvcResult = mockMvc.perform(patch("/api/authenticated/section")
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonPayload)
      .header("Authorization", "Bearer " + intruderToken)
      .header("Org-Authorization", intruderOrgHeader)
      .param("sectionId", section.getId().toString())
    )
    .andExpect(status().isNotFound())
    .andReturn();

    String responseBody = mvcResult.getResponse().getContentAsString();
    log.info("Response Body: {}", responseBody);
  }

  @Test 
  void orgMemberCannotUpdatePublicUsersSection() throws Exception {
    String jsonPayload = """
    {
      "name": "John",
      "description": "Smith",
      "isActive": true
    }
    """;

    User user = userHelper.createUser();

    Section section = topicHelper.createSection("Random section", null, user);

    User intruderUser = userHelper.createUser();
    String intruderToken = userHelper.getToken(intruderUser);
    OrganizationMember intruderAdmin = orgHelper.createOrgWithMember(intruderUser, OrgRoles.ORG_ADMIN, null);
    String intruderOrgHeader = orgHelper.getOrgHeader(intruderToken, intruderAdmin.getOrganization().getId());

    MvcResult mvcResult = mockMvc.perform(patch("/api/authenticated/section")
      .contentType(MediaType.APPLICATION_JSON)
      .content(jsonPayload)
      .header("Authorization", "Bearer " + intruderToken)
      .header("Org-Authorization", intruderOrgHeader)
      .param("sectionId", section.getId().toString())
    )
    .andExpect(status().isNotFound())
    .andReturn();

    String responseBody = mvcResult.getResponse().getContentAsString();
    log.info("Response Body: {}", responseBody);
  }
}
