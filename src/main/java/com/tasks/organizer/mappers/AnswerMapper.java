package com.tasks.organizer.mappers;

import com.tasks.organizer.dto.response.AnswerResponse;
import com.tasks.organizer.dto.response.AnswerResponseWithCorrect;
import com.tasks.organizer.entities.Answer;

import org.mapstruct.IterableMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AnswerMapper {
    @Named ("student")
    @IterableMapping (qualifiedByName = "student")
    AnswerResponse toResponse(Answer answer);
    @Named ("student")
    @IterableMapping (qualifiedByName = "student")
    List<AnswerResponse> toResponseList(List<Answer> answers);

    @Named ("admin")
    @IterableMapping(qualifiedByName = "admin")
    AnswerResponseWithCorrect toAdminAnswerRepsonse(Answer answer);
    @Named ("admin")
    @IterableMapping(qualifiedByName = "admin")
    List<AnswerResponseWithCorrect> toAdminAnswerResponseList(List<Answer> answer);
}