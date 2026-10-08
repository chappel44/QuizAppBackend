package com.tasks.organizer.controller;

import com.tasks.organizer.repository.SectionRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tasks.organizer.controller.AdminController.ApiResponse;
import com.tasks.organizer.dto.response.SectionOverviewResponse;
import com.tasks.organizer.entities.Role;
import com.tasks.organizer.entities.Section;
import com.tasks.organizer.entities.User;
import com.tasks.organizer.mappers.SectionMapperOverview;
import com.tasks.organizer.mappers.TopicMapper;
import com.tasks.organizer.repository.TopicRepository;
import com.tasks.organizer.service.TopicService;

import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collector;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;


@RestController 
@RequestMapping ("/api/authenticated")
@AllArgsConstructor 
public class AuthenticatedController {
  private final SectionRepository sectionRepository;
  final TopicRepository topicRepository;
  final TopicMapper topicMapper;
  final SectionMapperOverview sectionMapperOverview;
  final TopicService topicService;

  @GetMapping("/section-overview")
  public ResponseEntity getSectionsWithTopics() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    User user = (User) authentication.getPrincipal();

    List<Section> sections = new ArrayList<>();

    if(user.getRole().equals(Role.STUDENT)){
      sections = sectionRepository.findAllWithTopicsStudent();
    }
    else if (user.getRole().equals(Role.ADMIN)) {
      sections = sectionRepository.findAllWithTopicsAdmin();
    }
    
    List<SectionOverviewResponse> overview = sectionMapperOverview.toResponse(sections);

    return ResponseEntity
    .status(HttpStatus.OK)
    .body(new ApiResponse<>(
      200,
      "Section overview",
      overview
    ));
  }

  @GetMapping ("/topics")
  public ResponseEntity getTopicWithQuestionsAndAnswers(@RequestParam UUID topicId) {
    return topicService.getTopicWithQuestionsAndAnswers(topicId);
  }
}
