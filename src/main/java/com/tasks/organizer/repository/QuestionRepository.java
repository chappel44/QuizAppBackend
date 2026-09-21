package com.tasks.organizer.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.tasks.organizer.entities.Question;
import com.tasks.organizer.entities.Topic;

public interface QuestionRepository extends JpaRepository<Question, UUID>{
  Optional<Question> findById(UUID id);
  Optional<List<Question>> findAllByTopic(Topic topic);
  
  @Query("""
    SELECT DISTINCT q FROM Question q
    LEFT JOIN FETCH q.answers
    WHERE q.topic.id = :topicId
    """)
  List<Question> findByTopicIdWithAnswers(@Param("topicId") UUID topicId);
}
