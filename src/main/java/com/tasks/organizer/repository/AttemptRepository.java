package com.tasks.organizer.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tasks.organizer.entities.Attempt;

public interface AttemptRepository extends JpaRepository<Attempt, UUID> {
  Optional<Attempt> findById(UUID id);
}
