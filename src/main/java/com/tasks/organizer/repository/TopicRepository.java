package com.tasks.organizer.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tasks.organizer.entities.Topic;

public interface TopicRepository extends JpaRepository<Topic, UUID> {
  Optional<Topic> findById(UUID id);
}
