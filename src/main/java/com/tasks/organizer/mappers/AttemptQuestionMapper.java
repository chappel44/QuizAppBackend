package com.tasks.organizer.mappers;

import com.tasks.organizer.dto.response.AttemptQuestionResponse;
import com.tasks.organizer.entities.AttemptQuestion;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AttemptQuestionMapper {
  @Mapping(target = "question", source = "question")
  AttemptQuestionResponse toResponse(AttemptQuestion attemptQuestion);

  @Mapping (target = "question", source = "question")
  List<AttemptQuestionResponse> toResponseList(List<AttemptQuestion> attemptQuestions);
}