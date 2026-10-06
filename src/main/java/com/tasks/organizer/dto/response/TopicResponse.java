package com.tasks.organizer.dto.response;

import java.util.List;

import com.tasks.organizer.dto.response.base.BaseTopic;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@SuperBuilder @Getter @Setter 
public class TopicResponse extends BaseTopic {
  private List<QuestionResponse> questions;
}

