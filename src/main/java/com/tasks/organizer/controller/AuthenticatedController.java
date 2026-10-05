package com.tasks.organizer.controller;

import com.tasks.organizer.repository.SectionRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tasks.organizer.controller.AdminController.ApiResponse;
import com.tasks.organizer.dto.response.SectionOverviewResponse;
import com.tasks.organizer.entities.Section;
import com.tasks.organizer.mappers.SectionMapperOverview;
import com.tasks.organizer.mappers.TopicMapper;
import com.tasks.organizer.repository.TopicRepository;

import lombok.AllArgsConstructor;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;


@RestController 
@RequestMapping ("/api/authenticated")
@AllArgsConstructor 
public class AuthenticatedController {
  private final SectionRepository sectionRepository;
  final TopicRepository topicRepository;
  final TopicMapper topicMapper;
  final SectionMapperOverview sectionMapperOverview;

  @GetMapping("/section-overview")
  public ResponseEntity getSectionsWithTopics() {
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
