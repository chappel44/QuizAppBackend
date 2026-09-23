package com.tasks.organizer.dto.response;

import java.util.UUID;

import com.tasks.organizer.entities.Topic.TopicType;

import lombok.Builder;
import lombok.Getter;

@Builder @Getter 
public class TopicOverviewResponse {
  private UUID id;
  private String name;
  private String description;
  private TopicType topic;
}
