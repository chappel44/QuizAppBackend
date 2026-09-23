package com.tasks.organizer.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tasks.organizer.entities.AttemptQuestion;

public interface AttemptQuestionRepository extends JpaRepository<AttemptQuestion, UUID> {
  Optional<AttemptQuestion> findById(UUID attemptQuestionId);

  // @Query("""
  //   SELECT DISTINCT aq FROM AttemptQuestion aq
  //   LEFT JOIN FETCH aq.question
  //   WHERE aq.attempt.id = attemptId
  // """)
  // List<AttemptQuestion> findByAttemptQuestionWithQuestion(@Param ("attemptId") UUID attemptId);
}
