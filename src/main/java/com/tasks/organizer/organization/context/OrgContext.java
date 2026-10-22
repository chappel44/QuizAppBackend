package com.tasks.organizer.organization.context;

import java.util.Set;
import java.util.UUID;

import com.tasks.organizer.organizationmember.entity.OrgPermissions;
import com.tasks.organizer.organizationmember.entity.OrgRoles;

public record OrgContext(UUID organizationId, OrgRoles role, Set<OrgPermissions> permissions) {
  public boolean has(OrgPermissions p) {
    return role == OrgRoles.ORG_ADMIN || permissions.contains(p);
  }
}