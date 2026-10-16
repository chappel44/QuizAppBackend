package com.tasks.organizer.attempt.mapper;

import com.tasks.organizer.attempt.dto.response.AttemptResponse;
import com.tasks.organizer.attempt.entity.Attempt;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring", uses = { AttemptQuestionMapper.class })
public interface AttemptMapper {
  @Mapping (source = "finalized", target = "isFinalized")
  AttemptResponse toResponse(Attempt attempt); //AttemptResponse -> AttemptQuestionResponse

  List<AttemptResponse> toResponseList(List<Attempt> attempt);
}