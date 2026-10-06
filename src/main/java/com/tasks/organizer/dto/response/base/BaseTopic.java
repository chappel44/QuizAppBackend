package com.tasks.organizer.dto.response.base;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import com.tasks.organizer.entities.Topic.TopicType;

import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@MappedSuperclass 
@Getter @Setter @SuperBuilder (toBuilder = true)
public class BaseTopic {
  private UUID id;
  private Instant createdAt;
  private String name;

  private String description;

  private TopicType topicType;
  
  private LocalDateTime dueDate;

  private Boolean isActive;
}