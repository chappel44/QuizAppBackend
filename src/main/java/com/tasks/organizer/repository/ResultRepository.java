package com.tasks.organizer.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tasks.organizer.entities.Results;

import java.util.Optional;
import java.util.UUID;

public interface ResultRepository extends JpaRepository<Results, UUID> {
  Optional<Results> findById(UUID id);
}