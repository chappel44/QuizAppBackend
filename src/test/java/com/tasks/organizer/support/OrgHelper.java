package com.tasks.organizer.support;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tasks.organizer.organization.OrganizationRepository;
import com.tasks.organizer.organization.entity.Organization;
import com.tasks.organizer.organizationmember.OrganizationMemberRepository;
import com.tasks.organizer.organizationmember.entity.OrgPermissions;
import com.tasks.organizer.organizationmember.entity.OrgRoles;
import com.tasks.organizer.organizationmember.entity.OrganizationMember;
import com.tasks.organizer.user.entity.User;

/**
 * Reusable org setup for integration tests.
 * Register it on a test class with {@code @Import(OrgHelper.class)}; the class must also use
 * {@code @AutoConfigureMockMvc} since {@link #getOrgHeader} goes through MockMvc.
 */
@TestComponent
public class OrgHelper {
  @Autowired MockMvc mockMvc;
  @Autowired ObjectMapper objectMapper;
  @Autowired OrganizationRepository organizationRepository;
  @Autowired OrganizationMemberRepository organizationMemberRepository;

  public String getOrgHeader(String userToken, UUID organizationId) throws Exception {
    MvcResult result = mockMvc.perform(
        post("/api/authenticated/organization/{organizationId}/create-org-token", organizationId)
          .header("Authorization", "Bearer " + userToken))
      .andExpect(status().isOk())
      .andReturn();

    String token = objectMapper.readTree(result.getResponse().getContentAsString())
      .path("data").path("token").asText();

    return "Bearer " + token;
  }

  public Organization createOrganization(String name) {
    return organizationRepository.save(Organization.builder()
      .name(name + " " + UUID.randomUUID())
      .description(name)
      .maxOrgAdmins(5)
      .maxTeachers(5)
      .maxStudents(30)
      .build());
  }

  public OrganizationMember addMember(Organization org, User user, OrgRoles role, boolean active, OrgPermissions... permissions) {
    return organizationMemberRepository.save(OrganizationMember.builder()
      .user(user)
      .role(role)
      .organization(org)
      .permissions(permissions)   // varargs arrives as an array; empty if none passed
      .isActive(active)
      .build());
  }

  // Convenience: new org + active member in one call
  public OrganizationMember createOrgWithMember(User user, OrgRoles role, OrgPermissions... permissions) {
    Organization org = createOrganization("Good guys");
    return addMember(org, user, role, true, permissions);
  }
}
