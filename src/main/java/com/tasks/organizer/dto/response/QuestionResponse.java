package com.tasks.organizer.dto.response;

import java.util.List;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @Builder 
public class QuestionResponse {
  private UUID id;

  private int points;

  private String question;

  //private String answer;

  private String imageUrl;

  private List<AnswerResponse> answers;
}
