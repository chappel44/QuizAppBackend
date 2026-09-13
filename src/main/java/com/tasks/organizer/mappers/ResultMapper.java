package com.tasks.organizer.mappers;

import java.util.List;

import org.mapstruct.Mapper;

import com.tasks.organizer.dto.response.ResultsResponse;
import com.tasks.organizer.entities.Results;

@Mapper (componentModel = "spring")
public interface ResultMapper {
  ResultsResponse toResponse(Results result);

  List<ResultsResponse> toResponseList(List<Results> results);
}
