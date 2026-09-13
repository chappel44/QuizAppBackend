package com.tasks.organizer.dto.response;

import java.time.Instant;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter @ToString @Builder 
public class AnswerResponse {
  private UUID id;
  private Instant createdAt;
  private String answer;
  private Boolean isCorrect;
}
