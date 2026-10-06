package com.tasks.organizer.mappers;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import com.tasks.organizer.dto.response.AdminTopicResponse;
import com.tasks.organizer.dto.response.TopicOverviewResponse;
import com.tasks.organizer.dto.response.TopicResponse;
import com.tasks.organizer.entities.Question;
import com.tasks.organizer.entities.Topic;

@Mapper  (componentModel = "spring", uses = QuestionMapper.class)
public interface TopicMapper {
    @Named ("student")
    TopicResponse toResponse(Topic topic);

    @Named ("student")
    @Mapping (target = "questions", source = "questions")
    TopicResponse toResponse(Topic topic, List<Question> questions);

    @Named ("student")
    List<TopicResponse> toResponseList(List<Topic> topics);
    
    @Named ("student")
    TopicOverviewResponse toResponseDescriptive(Topic topic);
    
    @Named ("admin")
    AdminTopicResponse toAdminTopicResponse(Topic topic);
}