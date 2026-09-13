package com.tasks.organizer.dto.response;

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
