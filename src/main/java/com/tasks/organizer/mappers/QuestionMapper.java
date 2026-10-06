package com.tasks.organizer.mappers;

import java.util.List;

import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import com.tasks.organizer.dto.response.AdminQuestionResponse;
import com.tasks.organizer.dto.response.IdResponse;
import com.tasks.organizer.dto.response.QuestionResponse;
import com.tasks.organizer.entities.Question;

@Mapper(componentModel = "spring", uses = AnswerMapper.class)
public interface QuestionMapper {
    @Named ("student")
    QuestionResponse toResponse(Question question);
    @Named ("student")
    @IterableMapping (qualifiedByName = "student")
    List<QuestionResponse> toResponseList(List<Question> questions);

    @Named ("student")
    IdResponse toIdResponse(Question topic);
    @Named ("student")
    @IterableMapping (qualifiedByName = "student")
    List<IdResponse> toIdResponseList(List<Question> topics);

    @Named ("admin")
    AdminQuestionResponse toAdminQuestionResponse(Question question);
    @Named ("admin")
    @IterableMapping(qualifiedByName = "admin")
    List<AdminQuestionResponse> toAdminQuestionResponseList(List<Question> question);
}