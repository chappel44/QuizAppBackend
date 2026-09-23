package com.tasks.organizer.dto.response;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.tasks.organizer.entities.Topic.TopicType;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Builder @Setter 
public class TopicResponse {
  private UUID id;
  private Instant createdAt;
  private String name;

  private String description;

  private TopicType topicType;
  
  private LocalDateTime dueDate;

  private Boolean isActive;

  private List<QuestionResponse> questions;
}
