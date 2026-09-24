package com.tasks.organizer.service;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

public interface StudentService {
  ResponseEntity createAttempt(UUID topicId);
  ResponseEntity recordAttemptQuestion(UUID answerId, UUID attemptQuestionId);
  ResponseEntity gradeAttempt(UUID attemptId);
  ResponseEntity getAttempt(UUID attemptId);
  ResponseEntity getTopicAndAttempts(UUID topicId);
}
