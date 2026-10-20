package com.tasks.organizer.topic.dto.response;

import com.tasks.organizer.question.dto.response.AdminQuestionResponse;

import java.util.List;
import java.util.UUID;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@ToString
@SuperBuilder @Getter @Setter 
public class AdminTopicResponse extends BaseTopic{
  UUID sectionId;
  List<AdminQuestionResponse> questions;
  Integer questionPoolSize;
  private boolean isActive;
}
