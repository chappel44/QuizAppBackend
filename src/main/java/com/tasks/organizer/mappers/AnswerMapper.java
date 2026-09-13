package com.tasks.organizer.mappers;

import com.tasks.organizer.dto.response.AnswerResponse;
import com.tasks.organizer.entities.Answer;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AnswerMapper {

    AnswerResponse toResponse(Answer answer);

    List<AnswerResponse> toResponseList(List<Answer> answers);
}