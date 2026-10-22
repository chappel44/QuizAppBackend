package com.tasks.organizer.organization.context;

import java.util.Collection;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class OrgAuthenticationToken extends UsernamePasswordAuthenticationToken {
  private final OrgContext orgContext; // null when no org token was sent

  public OrgAuthenticationToken(UserDetails principal, Object credentials,
      Collection<? extends GrantedAuthority> authorities, OrgContext orgContext) {
    super(principal, credentials, authorities);
    this.orgContext = orgContext;
  }

  public OrgContext getOrgContext() { return orgContext; }
}
