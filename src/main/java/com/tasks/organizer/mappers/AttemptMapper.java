package com.tasks.organizer.mappers;

import com.tasks.organizer.dto.response.AttemptResponse;
import com.tasks.organizer.entities.Attempt;

import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring", uses = { AttemptQuestionMapper.class })
public interface AttemptMapper {
  AttemptResponse toResponse(Attempt attempt); //AttemptResponse -> AttemptQuestionResponse

  List<AttemptResponse> toResponseList(List<Attempt> attempt);
}