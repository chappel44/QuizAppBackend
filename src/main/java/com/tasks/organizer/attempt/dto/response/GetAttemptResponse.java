package com.tasks.organizer.attempt.dto.response;

import com.tasks.organizer.topic.dto.response.TopicResponse;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Builder;
import lombok.Getter;

@Builder @JsonSerialize @Getter 
public class GetAttemptResponse {
  AttemptResponse attempt;
  TopicResponse topic;
}
