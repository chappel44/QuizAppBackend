package com.tasks.organizer.organizationmember.service;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.tasks.organizer.authentication.OrgSignupTokenRepository;
import com.tasks.organizer.authentication.entity.OrgSignupToken;
import com.tasks.organizer.common.dto.ApiResponse;
import com.tasks.organizer.organization.OrganizationRepository;
import com.tasks.organizer.organization.entity.Organization;
import com.tasks.organizer.organizationmember.dto.request.OrgSignupTokenDTO;
import com.tasks.organizer.organizationmember.entity.OrgPermissions;
import com.tasks.organizer.organizationmember.entity.OrgRoles;
import com.tasks.organizer.organizationmember.entity.OrganizationMember;
import com.tasks.organizer.user.entity.User;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class OrganizationMemberServiceImpl implements OrganizationMemberService {
  final OrganizationRepository organizationRepository;
  final OrgSignupTokenRepository orgSignupTokenRepository;

  private static final Duration SIGNUP_TOKEN_TTL = Duration.ofDays(7);

  @Override
  @Transactional
  public ResponseEntity<ApiResponse<Object>> createOrgSignupToken(OrgSignupTokenDTO request, UUID organizationId) {
    OrgRoles role;
    try{
      role = OrgRoles.valueOf(request.getRole().trim().toUpperCase());
    }
    catch(IllegalArgumentException e){
      return ApiResponse.error(HttpStatus.BAD_REQUEST, "Invalid role " + request.getRole() + ". Roles can only be ORG_ADMIN, TEACHER, or STUDENT");
    }

    Organization organization = organizationRepository.findById(organizationId).orElse(null);
    if(organization == null){
      return ApiResponse.error(HttpStatus.NOT_FOUND, "Organization not found " + organizationId);
    }

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    User user = (User) authentication.getPrincipal();

    List<OrganizationMember> members = organization.getOrganizationMembers();

    //Only an active ORG_ADMIN of this organization can invite new members
    boolean isOrgAdmin = members.stream()
      .anyMatch(m -> m.getUser() != null
        && Objects.equals(m.getUser().getId(), user.getId())
        && m.getRole() == OrgRoles.ORG_ADMIN
        && m.isActive());
    if(!isOrgAdmin){
      return ApiResponse.error(HttpStatus.FORBIDDEN, "User is not an admin of organization " + organizationId);
    }

    String inviteeEmail = request.getInviteeEmail().trim();
    boolean alreadyMember = members.stream()
      .anyMatch(m -> m.getUser() != null && inviteeEmail.equalsIgnoreCase(m.getUser().getEmail()));
    if(alreadyMember){
      return ApiResponse.error(HttpStatus.CONFLICT, inviteeEmail + " is already a member of this organization");
    }

    if(role == OrgRoles.STUDENT){
      long studentCount = members.stream().filter(m -> m.getRole() == OrgRoles.STUDENT).count();
      if(studentCount >= organization.getMaxStudents()){
        return ApiResponse.error(HttpStatus.CONFLICT, "Organization has reached its maximum of " + organization.getMaxStudents() + " students");
      }
    }
    else if(role == OrgRoles.TEACHER){
      long teacherCount = members.stream().filter(m -> m.getRole() == OrgRoles.TEACHER).count();
      if(teacherCount >= organization.getMaxTeachers()){
        return ApiResponse.error(HttpStatus.CONFLICT, "Organization has reached its maximum of " + organization.getMaxTeachers() + " teachers");
      }
    }
    else {
      long orgAdminCount = members.stream().filter(m -> m.getRole() == OrgRoles.ORG_ADMIN).count();
      if(orgAdminCount >= organization.getMaxOrgAdmins()){
        return ApiResponse.error(HttpStatus.CONFLICT, "Organization has reached its maximum of " + organization.getMaxOrgAdmins() + " organization admins");
      }
    }

    //Re-inviting an email replaces any existing signup token for this organization
    List<OrgSignupToken> existingTokens = orgSignupTokenRepository.findAllByOrganizationId(organizationId).stream()
      .filter(t -> inviteeEmail.equalsIgnoreCase(t.getInviteeEmail()))
      .toList();
    orgSignupTokenRepository.deleteAll(existingTokens);

    //Permissions only apply to TEACHER, STUDENT and ORG_ADMIN always get none
    OrgPermissions[] permissions = new OrgPermissions[0];
    if(role == OrgRoles.TEACHER){
      List<String> invalidPermissions = new ArrayList<>();
      Set<OrgPermissions> parsedPermissions = new LinkedHashSet<>();
      String rawPermissions = request.getPermissions() == null ? "" : request.getPermissions();

      for(String permission : rawPermissions.split(",")){
        String trimmed = permission.trim();
        if(trimmed.isEmpty()){
          continue;
        }
        try{
          parsedPermissions.add(OrgPermissions.valueOf(trimmed));
        }
        catch(IllegalArgumentException e){
          invalidPermissions.add(trimmed);
        }
      }

      if(!invalidPermissions.isEmpty()){
        return ApiResponse.error(HttpStatus.BAD_REQUEST,
          "Invalid permissions " + invalidPermissions + ". Valid permissions are " + Arrays.toString(OrgPermissions.values()));
      }
      permissions = parsedPermissions.toArray(OrgPermissions[]::new);
    }

    OrgSignupToken token = orgSignupTokenRepository.save(OrgSignupToken.builder()
      .organization(organization)
      .inviteeEmail(inviteeEmail)
      .role(role)
      .permissions(permissions)
      .expiresAt(Instant.now().plus(SIGNUP_TOKEN_TTL))
      .build());

    return ApiResponse.of(HttpStatus.CREATED, "Signup token created", Map.of(
      "token", token.getId(),
      "inviteeEmail", token.getInviteeEmail(),
      "role", token.getRole(),
      "permissions", token.getPermissions(),
      "expiresAt", token.getExpiresAt()
    ));
  }

  @Override
  @Transactional
  public ResponseEntity<ApiResponse<Object>> redeemOrgSignupToken(UUID tokenId) {
    OrgSignupToken token = orgSignupTokenRepository.findById(tokenId).orElse(null);
    if(token == null){
      return ApiResponse.error(HttpStatus.NOT_FOUND, "Signup token not found " + tokenId);
    }

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    User user = (User) authentication.getPrincipal();

    if(!token.getInviteeEmail().equalsIgnoreCase(user.getEmail())){
      return ApiResponse.error(HttpStatus.FORBIDDEN, "This signup token was not issued to " + user.getEmail());
    }

    if(token.getExpiresAt().isBefore(Instant.now())){
      return ApiResponse.error(HttpStatus.GONE, "Signup token has expired");
    }

    Organization organization = token.getOrganization();
    boolean alreadyMember = organization.getOrganizationMembers().stream()
      .anyMatch(m -> m.getUser() != null && Objects.equals(m.getUser().getId(), user.getId()));
    if(alreadyMember){
      return ApiResponse.error(HttpStatus.CONFLICT, "User is already a member of organization " + organization.getId());
    }

    OrganizationMember member = OrganizationMember.builder()
      .organization(organization)
      .user(user)
      .role(token.getRole())
      .permissions(token.getPermissions())
      .build();
    organization.getOrganizationMembers().add(member);
    organizationRepository.save(organization);

    //Signup tokens are single use
    orgSignupTokenRepository.delete(token);

    return ApiResponse.ok("Joined organization", Map.of(
      "organizationId", organization.getId(),
      "role", member.getRole(),
      "permissions", member.getPermissions()
    ));
  }
}
