package com.tasks.organizer.attempt.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tasks.organizer.attempt.entity.Attempt;
import com.tasks.organizer.attempt.entity.AttemptQuestion;

public interface AttemptQuestionRepository extends JpaRepository<AttemptQuestion, UUID> {
  Optional<AttemptQuestion> findById(UUID attemptQuestionId);

  Optional<List<AttemptQuestion>> findByAttempt(Attempt attempt);

  // @Query("""
  //   SELECT DISTINCT aq FROM AttemptQuestion aq
  //   LEFT JOIN FETCH aq.question
  //   WHERE aq.attempt.id = attemptId
  // """)
  // List<AttemptQuestion> findByAttemptQuestionWithQuestion(@Param ("attemptId") UUID attemptId);
}
