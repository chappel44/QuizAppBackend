package com.tasks.organizer.mappers;

import com.tasks.organizer.dto.response.AttemptResponse;
import com.tasks.organizer.entities.Attempt;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = { AttemptQuestionMapper.class })
public interface AttemptMapper {
  @Mapping (source = "finalized", target = "isFinalized")
  AttemptResponse toResponse(Attempt attempt); //AttemptResponse -> AttemptQuestionResponse

  List<AttemptResponse> toResponseList(List<Attempt> attempt);
}