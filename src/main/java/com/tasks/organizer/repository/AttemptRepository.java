package com.tasks.organizer.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.tasks.organizer.entities.Attempt;

public interface AttemptRepository extends JpaRepository<Attempt, UUID> {
  Optional<Attempt> findById(UUID id);

  @Query("""
  SELECT DISTINCT att FROM Attempt att
  LEFT JOIN FETCH att.topic t
  LEFT JOIN FETCH att.attemptQuestions aq
  LEFT JOIN FETCH aq.question q
  WHERE att.id = :attemptId
  """)
  Optional<Attempt> findByTopicIdWithAnswers(@Param("attemptId") UUID attemptId);

  Optional<List<Attempt>> findByUserIdAndTopicId(UUID userId, UUID topicId);
}
