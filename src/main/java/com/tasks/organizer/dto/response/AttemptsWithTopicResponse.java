package com.tasks.organizer.dto.response;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor  @Getter @Setter 
public class AttemptsWithTopicResponse {
  List<AttemptResponse> attempts;
  TopicResponse topic;
}
