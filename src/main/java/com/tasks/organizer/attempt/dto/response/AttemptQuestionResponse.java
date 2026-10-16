package com.tasks.organizer.attempt.dto.response;

import com.tasks.organizer.question.dto.response.QuestionResponse;

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
