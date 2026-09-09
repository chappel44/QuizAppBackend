package com.tasks.organizer.repository;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tasks.organizer.entities.Section;

public interface SectionRepository extends JpaRepository<Section, UUID>{
  Optional<Section> findById(UUID id);
}
