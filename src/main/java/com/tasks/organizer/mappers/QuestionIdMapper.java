package com.tasks.organizer.mappers;

import java.util.List;
//import java.util.UUID;

import org.mapstruct.Mapper;

import com.tasks.organizer.dto.response.IdResponse;
import com.tasks.organizer.entities.Question;

@Mapper (componentModel = "spring")
public interface QuestionIdMapper {
    IdResponse toResponse(Question topic);

    List<IdResponse> toResponseList(List<Question> topics);
}
