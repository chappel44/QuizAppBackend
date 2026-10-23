package com.tasks.organizer.authorization;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.tasks.organizer.organization.context.OrgAuthenticationToken;
import com.tasks.organizer.organization.context.OrgContext;
import com.tasks.organizer.organizationmember.OrganizationMemberRepository;
import com.tasks.organizer.organizationmember.entity.OrgPermissions;
import com.tasks.organizer.user.entity.User;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor 
public class OrgAuthorization {
  private final OrganizationMemberRepository organizationMemberRepository;

  public OrgContext currentOrgContext() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    return (auth instanceof OrgAuthenticationToken t) ? t.getOrgContext() : null;
  }

  private User currentUser() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    return (auth != null && auth.getPrincipal() instanceof User u) ? u : null;
  }

  private boolean tokenMatches(UUID organizationId) {
    OrgContext org = currentOrgContext();
    return org != null
        && organizationId != null
        && org.organizationId().equals(organizationId);
  }

  /** Cheap check from token claims only. Fine for reads and low-risk actions. */
  public boolean hasPermission(UUID organizationId, OrgPermissions permission) {
    return tokenMatches(organizationId) && currentOrgContext().has(permission);
  }

  /** Token claims plus a live DB check that the membership is still active. Use for destructive actions. */
  public boolean hasPermissionVerified(UUID organizationId, OrgPermissions permission) {
    User user = currentUser();
    return user != null
        && hasPermission(organizationId, permission)
        && organizationMemberRepository
            .existsByOrganizationIdAndUserIdAndIsActiveTrue(organizationId, user.getId());
  }

  public boolean isStaff(UUID organizationId) {
    return tokenMatches(organizationId) && currentOrgContext().isStaff();
  }

  public boolean isStudent(UUID organizationId) {
    return tokenMatches(organizationId) && currentOrgContext().isStudent();
  }
}