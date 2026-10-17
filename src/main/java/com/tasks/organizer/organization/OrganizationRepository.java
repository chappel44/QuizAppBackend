package com.tasks.organizer.organization;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tasks.organizer.organization.entity.Organization;

public interface OrganizationRepository extends JpaRepository<Organization, UUID> {
}