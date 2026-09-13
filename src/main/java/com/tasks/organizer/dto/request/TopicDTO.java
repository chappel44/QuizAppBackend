package com.tasks.organizer.dto.request;

import java.time.LocalDateTime;
import java.util.UUID;

import com.tasks.organizer.entities.Topic.TopicType;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString 
public class TopicDTO {
  
  @NotNull 
  @Size (max = 50)
  private String name;

  @Size (max = 255)
  private String description;

  @NotNull 
  private TopicType topicType;
  
  private LocalDateTime dueDate;

  private Boolean isActive;

  private UUID sectionId;
}
