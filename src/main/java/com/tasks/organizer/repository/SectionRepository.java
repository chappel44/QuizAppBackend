package com.tasks.organizer.repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.tasks.organizer.entities.Section;

public interface SectionRepository extends JpaRepository<Section, UUID>{
  Optional<Section> findById(UUID id);

  @Query("""
    SELECT DISTINCT s FROM Section s
    LEFT JOIN FETCH s.topics
    """)
  List<Section> findAllWithTopics();
}
