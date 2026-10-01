package com.tasks.organizer.dto.response;

import java.util.List;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter @Setter @Builder @ToString 
public class AttemptResponse {
  UUID id;
  double totalPoints;
  double pointsEarned;
  double percentage;
  boolean isFinalized;
  List<AttemptQuestionResponse> attemptQuestions;
}
