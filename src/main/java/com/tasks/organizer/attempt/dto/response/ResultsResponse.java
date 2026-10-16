package com.tasks.organizer.attempt.dto.response;

import com.tasks.organizer.question.dto.response.IdResponse;

import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Builder @Setter 
public class ResultsResponse {
  private List<IdResponse> correctIds;

  private double pointsEarned;

  private double totalPoints;

  private double percentage;
}
