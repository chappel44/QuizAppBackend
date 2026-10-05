package com.tasks.organizer.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tasks.organizer.dto.request.CreateTopicDTO;
import com.tasks.organizer.dto.request.SectionDTO;
import com.tasks.organizer.entities.Section;
import com.tasks.organizer.repository.SectionRepository;
import com.tasks.organizer.repository.TopicRepository;
import com.tasks.organizer.service.TopicService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController 
@RequiredArgsConstructor
@RequestMapping ("/api/admin")
public class AdminController {
  public record ApiResponse<T>(
      int status,
      String message,
      T data
  ) {}

  private final TopicRepository topicRepository;
  private final SectionRepository sectionRepository;

  private final TopicService topicService;

  /* ========== Topic Mappings ========== */
  @PostMapping("/topics")
  public String createTopic(@Validated @RequestBody CreateTopicDTO entity, @RequestParam(required = true) UUID sectionId) {
    return topicService.generateTopic(entity, sectionId);
  }

  @PatchMapping ("/topics")
  public ResponseEntity updateTopic(@RequestParam UUID topicId, @Validated @RequestBody CreateTopicDTO request){
    return topicService.updateTopic(topicId, request);
  }
  
  @DeleteMapping ("/topics")
  public String deleteTopic(@RequestParam @NotNull UUID topicId) {
    if(topicId == null){
      return "Must provide a topic id";
    }
    topicRepository.deleteById(topicId);
    return "Topic deleted";
  }

  /* ========== Section Mappings ========== */
  @PostMapping("/section")
  public Section addSection(@Validated @RequestBody SectionDTO request) {
      
    Section newSection = Section.builder()
    .name(request.getName())
    .description(request.getDescription()).build();

    if(newSection == null){
      return new Section();
    }

    Section insertedSection = sectionRepository.save(newSection);

    return insertedSection;
  }

  @DeleteMapping ("/section")
  public String deleteSection(@RequestParam  @NotNull UUID sectionId){
    if(sectionId == null){
      return "Section id is required";
    }

    sectionRepository.deleteById(sectionId);
    return "Section deleted successfully";
  }
  
  @Transactional
  @PatchMapping ("/section")
  public ResponseEntity updateSection(
    @RequestParam @NotNull UUID sectionId,
    @RequestBody @NotNull SectionDTO request
  ){
    if(sectionId == null){
      return ResponseEntity
      .status(HttpStatus.BAD_REQUEST)
      .body(new ApiResponse<>(
        400,
        "Missing section id",
        null 
      ));
    }
    
    Optional<Section> sectionOpt = sectionRepository.findById(sectionId);

    if(!sectionOpt.isPresent()){
      return ResponseEntity
      .status(HttpStatus.NOT_FOUND)
      .body(new ApiResponse<>(
        404,
        "Section not found",
        null 
      ));
    }

    Section section = sectionOpt.get();

    section.setName(request.getName());
    section.setDescription(request.getDescription());
    section.setIsActive(request.isActive());

    return ResponseEntity
    .status(HttpStatus.OK)
    .body(new ApiResponse<>(
      200,
      "Section updated successfully",
      null 
    ));
  }
}
