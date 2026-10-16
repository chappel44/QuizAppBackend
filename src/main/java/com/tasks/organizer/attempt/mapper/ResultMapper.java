package com.tasks.organizer.attempt.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.tasks.organizer.attempt.dto.response.ResultsResponse;
import com.tasks.organizer.attempt.entity.Results;

@Mapper (componentModel = "spring")
public interface ResultMapper {
  ResultsResponse toResponse(Results result);

  List<ResultsResponse> toResponseList(List<Results> results);
}
