package com.tasks.organizer.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tasks.organizer.entities.Question;
import com.tasks.organizer.entities.Topic;

public interface QuestionRepository extends JpaRepository<Question, UUID>{
  Optional<Question> findById(UUID id);
  Optional<List<Question>> findAllByTopic(Topic topic);
}
