package com.tasks.organizer.authorization;

import java.util.UUID;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.tasks.organizer.organization.context.OrgAuthenticationToken;
import com.tasks.organizer.organization.context.OrgContext;
import com.tasks.organizer.organizationmember.entity.OrgPermissions;

@Component 
public class OrgAuthorization {

  /** Returns the org context from the current request's token, or null if none was sent. */
  public OrgContext currentOrgContext() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    return (auth instanceof OrgAuthenticationToken t) ? t.getOrgContext() : null;
  }

  /** True only if the token is for this org AND grants the permission. */
  public boolean hasPermission(UUID organizationId, OrgPermissions permission) {
    OrgContext org = currentOrgContext();
    return org != null
        && organizationId != null
        && org.organizationId().equals(organizationId)
        && org.has(permission);
  }
}
