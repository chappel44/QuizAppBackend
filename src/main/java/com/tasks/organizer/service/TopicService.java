package com.tasks.organizer.service;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.tasks.organizer.dto.request.CreateTopicDTO;
import com.tasks.organizer.dto.response.ApiResponse;

public interface TopicService {
  String generateTopic(CreateTopicDTO entity, UUID sectionId);
  ResponseEntity<ApiResponse<Object>> updateTopic(UUID topicId, CreateTopicDTO request);
  ResponseEntity<ApiResponse<Object>> getTopicWithQuestionsAndAnswers(UUID topicId);
  
}