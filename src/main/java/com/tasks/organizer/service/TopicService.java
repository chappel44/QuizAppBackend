package com.tasks.organizer.service;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.tasks.organizer.dto.request.CreateTopicDTO;

public interface TopicService {
  String generateTopic(CreateTopicDTO entity, UUID sectionId);

  ResponseEntity updateTopic(UUID topicId, CreateTopicDTO request);
}