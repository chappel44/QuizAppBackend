package com.tasks.organizer.dto.response;

import java.util.List;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @Builder 
public class AttemptResponse {
  UUID id;
  double totalPoints;
  double pointsEarned;
  double percentage;
  List<AttemptQuestionResponse> attemptQuestions;
}
