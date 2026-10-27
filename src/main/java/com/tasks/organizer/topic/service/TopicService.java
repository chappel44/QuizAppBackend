package com.tasks.organizer.topic.service;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.tasks.organizer.topic.dto.request.CreateTopicDTO;
import com.tasks.organizer.common.dto.ApiResponse;

public interface TopicService {
  ResponseEntity<ApiResponse<Object>> createTopic(CreateTopicDTO entity, UUID sectionId);
  ResponseEntity<ApiResponse<Object>> updateTopic(UUID topicId, CreateTopicDTO request);
  ResponseEntity<ApiResponse<Object>> getTopicWithQuestionsAndAnswers(UUID topicId);
  ResponseEntity<ApiResponse<Object>> deleteTopic(UUID topicId);
}