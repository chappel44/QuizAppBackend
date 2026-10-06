package com.tasks.organizer.dto.response;

import java.util.List;

import com.tasks.organizer.dto.response.base.BaseQuestion;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter @Setter @SuperBuilder
public class QuestionResponse extends BaseQuestion{
  private List<AnswerResponse> answers;
}
