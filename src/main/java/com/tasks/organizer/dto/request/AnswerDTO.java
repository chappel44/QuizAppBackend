package com.tasks.organizer.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@Builder 
@ToString 
public class AnswerDTO {
  @NotNull
  @Size (min=2, max = 255)
  private String answer;

  @NotNull 
  private Boolean isCorrect;

  private UUID id;
}
