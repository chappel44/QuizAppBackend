package com.tasks.organizer.dto.request;

import java.util.List;

import jakarta.validation.Valid;
import lombok.Getter;
import lombok.ToString;

@Getter 
@ToString 
public class CreateTopicDTO {
  @Valid
  private TopicDTO topic;
  
  @Valid
  private List<QuestionDTO> questions;
}
