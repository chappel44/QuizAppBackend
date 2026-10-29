package com.tasks.organizer.statistics.dto.response;

import java.util.List;
import java.util.Map;

import com.tasks.organizer.attempt.dto.response.TopicBest;
import com.tasks.organizer.topic.entity.Topic.TopicType;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder 
@Getter @Setter 
public class StudentAverageResponse {
  private Double studentAverage;

  private Map<TopicType, Double> averagesPerTopicType;

  private List<TopicBest> topicBest;
}
