package com.tasks.organizer.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.tasks.organizer.dto.response.TopicOverviewResponse;
import com.tasks.organizer.dto.response.TopicResponse;
import com.tasks.organizer.entities.Question;
import com.tasks.organizer.entities.Topic;

@Mapper  (componentModel = "spring", uses = QuestionMapper.class)
public interface TopicMapper {
    TopicResponse toResponse(Topic topic);

    @Mapping (target = "questions", source = "questions")
    TopicResponse toResponse(Topic topic, List<Question> questions);

    List<TopicResponse> toResponseList(List<Topic> topics);

    TopicOverviewResponse toResponseDescriptive(Topic topic);
}