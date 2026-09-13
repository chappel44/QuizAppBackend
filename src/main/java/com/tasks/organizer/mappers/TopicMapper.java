package com.tasks.organizer.mappers;

import java.util.List;

import org.mapstruct.Mapper;

import com.tasks.organizer.dto.response.TopicResponse;
import com.tasks.organizer.entities.Topic;

@Mapper  (componentModel = "spring", uses = QuestionMapper.class)
public interface TopicMapper {
    TopicResponse toResponse(Topic topic);

    List<TopicResponse> toResponseList(List<Topic> topics);
}