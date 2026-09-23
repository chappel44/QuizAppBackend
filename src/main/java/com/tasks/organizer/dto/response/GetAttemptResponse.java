package com.tasks.organizer.dto.response;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import lombok.Builder;
import lombok.Getter;

@Builder @JsonSerialize @Getter 
public class GetAttemptResponse {
  AttemptResponse attempt;
  TopicResponse topic;
}
