package com.tasks.organizer.controller;

import com.tasks.organizer.repository.SectionRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.tasks.organizer.controller.AdminController.ApiResponse;
import com.tasks.organizer.dto.response.SectionOverviewResponse;
import com.tasks.organizer.dto.response.TopicResponse;
import com.tasks.organizer.entities.Question;
import com.tasks.organizer.entities.Role;
import com.tasks.organizer.entities.Section;
import com.tasks.organizer.entities.Topic;
import com.tasks.organizer.entities.User;
import com.tasks.organizer.entities.Topic.TopicType;
import com.tasks.organizer.mappers.SectionMapperOverview;
import com.tasks.organizer.mappers.TopicMapper;
import com.tasks.organizer.repository.TopicRepository;

import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController 
@RequestMapping ("/api/authenticated")
@AllArgsConstructor 
public class AuthenticatedController {
  private final SectionRepository sectionRepository;
  final TopicRepository topicRepository;
  final TopicMapper topicMapper;
  final SectionMapperOverview sectionMapperOverview;

  @GetMapping("/topics")
  public ResponseEntity getMethodName(@RequestParam UUID topicId) {
    Topic topic = topicRepository.findById(topicId)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Topic not found" + topicId));
    
    if(topic.getTopicType().equals(TopicType.RANDOM_QUESTIONS)){
      List<Question> questionPool = new ArrayList<>(topic.getQuestions());
      
      while (questionPool.size() > topic.getQuestionPoolSize()) {
        int randomIndex = ThreadLocalRandom.current().nextInt(0, questionPool.size());
        questionPool.remove(randomIndex);
      }

      TopicResponse formattedTopic = topicMapper.toResponse(topic, questionPool);

      return ResponseEntity
      .status(HttpStatus.OK)
      .body(new ApiResponse<>(
        200,
        "Topic Retrieved",
        formattedTopic
      ));
    }
    
    TopicResponse formattedTopic = topicMapper.toResponse(topic);
    
    return ResponseEntity
    .status(HttpStatus.OK)
    .body(new ApiResponse<>(
      200,
      "Topic Retrieved",
      formattedTopic
    ));
  }

  @GetMapping("/section-overview")
  public ResponseEntity getMethodName() {
    List<Section> sections = sectionRepository.findAll();

    List<SectionOverviewResponse> overview = sectionMapperOverview.toResponse(sections);

    return ResponseEntity
    .status(HttpStatus.OK)
    .body(new ApiResponse<>(
          200,
          "Section overview",
          overview
      ));
  }
}
