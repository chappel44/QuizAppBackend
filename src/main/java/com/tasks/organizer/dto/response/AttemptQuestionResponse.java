package com.tasks.organizer.dto.response;

import java.util.UUID;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @Builder 
public class AttemptQuestionResponse {
  UUID id;
  Boolean isCorrect;
  UUID submittedAnswerId;
  QuestionResponse question;
}
