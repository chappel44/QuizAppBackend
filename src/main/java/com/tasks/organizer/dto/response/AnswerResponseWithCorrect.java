package com.tasks.organizer.dto.response;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@ToString 
@SuperBuilder 
@Getter @Setter 
public class AnswerResponseWithCorrect extends AnswerResponse{
  private boolean isCorrect;
}