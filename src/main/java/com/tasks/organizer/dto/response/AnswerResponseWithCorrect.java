package com.tasks.organizer.dto.response;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@SuperBuilder 
@Getter @Setter 
public class AnswerResponseWithCorrect extends AnswerResponse{
  private boolean isCorrect;
}