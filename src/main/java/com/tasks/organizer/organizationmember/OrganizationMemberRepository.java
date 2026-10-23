package com.tasks.organizer.organizationmember;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tasks.organizer.organizationmember.entity.OrganizationMember;

public interface OrganizationMemberRepository extends JpaRepository<OrganizationMember, UUID> {
  Optional<OrganizationMember> findByOrganizationIdAndUserId(UUID organizationId, UUID userId);

  boolean existsByOrganizationIdAndUserIdAndIsActiveTrue(UUID organizationId, UUID userId);
}
