package com.tasks.organizer.dto.response;

import java.util.List;

import com.tasks.organizer.dto.response.base.BaseQuestion;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@ToString 
@SuperBuilder @Getter @Setter 
public class AdminQuestionResponse extends BaseQuestion{
  List<AnswerResponseWithCorrect> answers;
  boolean isActive;
}
