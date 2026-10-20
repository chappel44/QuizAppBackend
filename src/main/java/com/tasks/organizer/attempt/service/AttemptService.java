package com.tasks.organizer.attempt.service;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.tasks.organizer.common.dto.ApiResponse;

public interface AttemptService {
  ResponseEntity<ApiResponse<Object>> createAttempt(UUID topicId);
  ResponseEntity<ApiResponse<Object>> recordAttemptQuestion(UUID answerId, UUID attemptQuestionId);
  ResponseEntity<ApiResponse<Object>> gradeAttempt(UUID attemptId);
  ResponseEntity<ApiResponse<Object>> getAttempt(UUID attemptId, UUID userId);
  ResponseEntity<ApiResponse<Object>> getTopicAndAttempts(UUID topicId, UUID userId);
}
