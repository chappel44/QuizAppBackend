package com.tasks.organizer.attempt;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tasks.organizer.attempt.entity.Results;

import java.util.Optional;
import java.util.UUID;

public interface ResultRepository extends JpaRepository<Results, UUID> {
  Optional<Results> findById(UUID id);
}