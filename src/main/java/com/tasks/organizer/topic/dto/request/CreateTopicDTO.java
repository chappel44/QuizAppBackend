package com.tasks.organizer.topic.dto.request;

import com.tasks.organizer.question.dto.request.QuestionDTO;

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
