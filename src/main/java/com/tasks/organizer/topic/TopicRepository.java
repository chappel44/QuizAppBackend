package com.tasks.organizer.topic;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tasks.organizer.topic.entity.Topic;

public interface TopicRepository extends JpaRepository<Topic, UUID> {
  Optional<Topic> findById(UUID id);
}
