package com.tasks.organizer.question.dto.response;

import java.util.List;


import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter @Setter @SuperBuilder
public class QuestionResponse extends BaseQuestion{
  private List<AnswerResponse> answers;
}
