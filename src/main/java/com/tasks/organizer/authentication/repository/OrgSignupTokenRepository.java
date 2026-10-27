package com.tasks.organizer.authentication.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tasks.organizer.authentication.entity.OrgSignupToken;

public interface OrgSignupTokenRepository extends JpaRepository<OrgSignupToken, UUID> {
  List<OrgSignupToken> findAllByOrganizationId(UUID organizationId);
}
