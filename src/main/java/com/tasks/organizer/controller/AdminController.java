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

import java.util.UUID;
//import static java.lang.System.out;

import org.springframework.http.ResponseEntity;
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

  /***
    Description: This is an API route for an admin to create a new topic entity.
    Expected params: sectionId,
    Expected body:
    {
      topic: {
        name: String,
        description: String,
        topicType : TEST | QUIZ | REVIEW | RANDOM_QUESTIONS, 
        dueDate: 2026-12-03T18:30:00,
        isActive: Boolean,
        questionPoolSize: Optional<Integer>(defaults to 1),
        sectionId: UUID
      },
      questions: [
        {
          id: Optional(UUID), if not provided the question is deleted and reinserted
          points: Decimal,
          question: String,
          answer: String,
          imageUrl: Optional(String)
          answers: [
            {
              answer: String,
              isCorrect: Boolean,
            }
          ]
        }
      ]
    }
  ***/
  @PostMapping("/topics")
  public String createTopic(@Validated @RequestBody CreateTopicDTO entity, @RequestParam(required = true) UUID sectionId) {
      return topicService.generateTopic(entity, sectionId);
  }

  /***
    Description: This is an API route for an admin to update a topic.
    Expected params: topicId,
    Expected body:
    {
      topic: {
        name: String,
        description: String,
        topicType : TEST | QUIZ | REVIEW | RANDOM_QUESTIONS, 
        dueDate: 2026-12-03T18:30:00,
        isActive: Boolean,
        questionPoolSize: Optional<Integer>(defaults to 1),
        sectionId: UUID
      },
      questions: [
        {
          id: Optional(UUID), if not provided the question is deleted and reinserted
          points: Decimal,
          question: String,
          answer: String,
          imageUrl: Optional(String)
          answers: [
            {
              id: Optional(String), if not provided answer is deleted and reinserted
              answer: String,
              isCorrect: Boolean,
            }
          ]
        }
      ]
    }
  ***/
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
}
