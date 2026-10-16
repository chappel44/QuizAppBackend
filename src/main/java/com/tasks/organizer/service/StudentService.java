package com.tasks.organizer.service;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.tasks.organizer.dto.response.ApiResponse;

public interface StudentService {
  ResponseEntity<ApiResponse<Object>> createAttempt(UUID topicId);
  ResponseEntity<ApiResponse<Object>> recordAttemptQuestion(UUID answerId, UUID attemptQuestionId);
  ResponseEntity<ApiResponse<Object>> gradeAttempt(UUID attemptId);
  ResponseEntity<ApiResponse<Object>> getAttempt(UUID attemptId);
  ResponseEntity<ApiResponse<Object>> getTopicAndAttempts(UUID topicId);
}
