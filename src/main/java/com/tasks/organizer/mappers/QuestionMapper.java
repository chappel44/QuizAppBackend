package com.tasks.organizer.mappers;

import java.util.List;
import org.mapstruct.Mapper;

import com.tasks.organizer.dto.response.IdResponse;
import com.tasks.organizer.dto.response.QuestionResponse;
import com.tasks.organizer.entities.Question;

@Mapper(componentModel = "spring")
public interface QuestionMapper {

    QuestionResponse toResponse(Question question);

    List<QuestionResponse> toResponseList(List<Question> questions);

    IdResponse toIdResponse(Question topic);

    List<IdResponse> toIdResponseList(List<Question> topics);
}