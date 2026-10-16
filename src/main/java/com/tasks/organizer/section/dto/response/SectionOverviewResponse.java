package com.tasks.organizer.section.dto.response;

import com.tasks.organizer.topic.dto.response.TopicOverviewResponse;

import java.util.List;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Builder @Getter 
public class SectionOverviewResponse {
  private UUID id;
  private String name;
  private String description;
  private List<TopicOverviewResponse> topics;  
}
