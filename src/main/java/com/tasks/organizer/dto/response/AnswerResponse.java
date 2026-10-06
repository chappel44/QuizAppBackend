package com.tasks.organizer.dto.response;

import java.time.Instant;
import java.util.UUID;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter @ToString 
@SuperBuilder (toBuilder = true)
@NoArgsConstructor 
public class AnswerResponse {
  private UUID id;
  private Instant createdAt;
  private String answer;
}