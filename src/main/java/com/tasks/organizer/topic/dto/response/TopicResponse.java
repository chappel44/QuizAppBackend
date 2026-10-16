package com.tasks.organizer.topic.dto.response;

import com.tasks.organizer.question.dto.response.QuestionResponse;

import java.util.List;


import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@SuperBuilder @Getter @Setter 
public class TopicResponse extends BaseTopic {
  private List<QuestionResponse> questions;
}

