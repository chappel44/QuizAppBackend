package com.tasks.organizer.attempt.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

import com.tasks.organizer.topic.entity.Topic.TopicType;

public record TopicBest(
  UUID topicId, 
  String name,
  int version,
  TopicType topicType,
  LocalDateTime dueDate,
  Double bestPercentage,
  Double totalPoints, 
  Double pointsEarned
) {}
