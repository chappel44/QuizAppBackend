package com.tasks.organizer.dto.request;

import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@ToString
@Builder 
public class QuestionDTO {

  @Min(0)
  private int points;

  @NotNull
  @Size (min = 2, max = 255)
  private String question;

  // @NotNull
  @Size (min=2, max = 255)
  private String answer;

  private List<AnswerDTO> answers;

  private UUID id;

  private String imageUrl;

  @Setter 
  private UUID correctAnswerId;
}