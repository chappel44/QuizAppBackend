package com.tasks.organizer.question.mapper;

import java.util.List;
//import java.util.UUID;

import org.mapstruct.Mapper;

import com.tasks.organizer.question.dto.response.IdResponse;
import com.tasks.organizer.question.entity.Question;

@Mapper (componentModel = "spring")
public interface QuestionIdMapper {
    IdResponse toResponse(Question topic);

    List<IdResponse> toResponseList(List<Question> topics);
}
