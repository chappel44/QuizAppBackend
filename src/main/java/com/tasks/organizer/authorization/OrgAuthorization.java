package com.tasks.organizer.authorization;

import java.nio.channels.AsynchronousByteChannel;
import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.AsyncServerResponse;

import com.tasks.organizer.organization.context.OrgAuthenticationToken;
import com.tasks.organizer.organization.context.OrgContext;
import com.tasks.organizer.organizationmember.OrganizationMemberRepository;
import com.tasks.organizer.organizationmember.entity.OrgPermissions;
import com.tasks.organizer.organizationmember.entity.OrgRoles;
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

  public User currentUser() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    return (auth != null && auth.getPrincipal() instanceof User u) ? u : null;
  }

  private boolean tokenMatches(UUID organizationId) {
    OrgContext org = currentOrgContext();

    if (org == null) {
      System.out.println("User is missing valid Org-Authorization header");
      return false;
    }
    if (organizationId == null) {
      System.out.println("Requested organization id is null");
      return false;
    }
    if (!org.organizationId().equals(organizationId)) {
      System.out.println("Organization ids don't match: requested=" + organizationId
          + " token=" + org.organizationId());
      return false;
    }
    if (!currentUser().getId().equals(org.userId())) {
      System.out.println("Org-Authorization token was issued for a different user: bearerUser="
          + currentUser().getId() + " tokenUser=" + org.userId());
      return false;
    }
    return true;
  }

  public boolean hasPermission(UUID organizationId, OrgPermissions permission) {
    if(currentOrgContext() != null && !currentOrgContext().has(permission)){
      System.out.println("User is missing the permission: " + permission);
      return false;
    }
    return tokenMatches(organizationId);
  }

  public boolean hasPermissionVerified(UUID organizationId, OrgPermissions permission) {
    User user = currentUser();
    return user != null
        && hasPermission(organizationId, permission)
        && organizationMemberRepository
            .existsByOrganizationIdAndUserIdAndIsActiveTrue(organizationId, user.getId());
  }
  
  /**
   * Ensures the authenticated user is staff for the given organization.
   *
   * <p>This check passes only if both of the following are true:
   * <ul>
   *   <li>The user has the role {@code OrgRoles.ORG_ADMIN} or {@code OrgRoles.TEACHER}.</li>
   *   <li>The organization ID in the {@code Org-Authorization} header matches
   *       the {@code organizationId} being accessed.</li>
   * </ul>
   *
   * @param organizationId the ID of the organization the user is trying to access
   * @return {@code true} if the user is an org admin or teacher in the organization
   *         identified by the {@code Org-Authorization} header, and that organization
   *         matches {@code organizationId}; {@code false} otherwise
  */
  public boolean isStaff(UUID organizationId) {
    return tokenMatches(organizationId) && currentOrgContext().isStaff();
  }

  public boolean isStudent(UUID organizationId) {
    boolean isStudent = tokenMatches(organizationId) && currentOrgContext().isStudent();
    if(tokenMatches(organizationId) && !currentOrgContext().isStudent()){
      System.out.println("User is not a student in the org");
    }
    return isStudent;
  }

  public boolean isOrgAdminOrHasPermission(UUID organizationId, OrgPermissions orgPermissions){

    return ( tokenMatches(organizationId) && currentOrgContext().role().equals(OrgRoles.ORG_ADMIN) ) 
    || ( hasPermission(organizationId, orgPermissions) && currentOrgContext().role().equals(OrgRoles.TEACHER) );
  }
}