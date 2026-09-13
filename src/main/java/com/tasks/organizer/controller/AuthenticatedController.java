package com.tasks.organizer.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.tasks.organizer.controller.AdminController.ApiResponse;
import com.tasks.organizer.dto.response.TopicResponse;
import com.tasks.organizer.entities.Topic;
import com.tasks.organizer.mappers.TopicMapper;
import com.tasks.organizer.repository.TopicRepository;

import lombok.AllArgsConstructor;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RestController 
@RequestMapping ("/api/authenticated")
@AllArgsConstructor 
public class AuthenticatedController {
  final TopicRepository topicRepository;
  final TopicMapper topicMapper;
  @GetMapping("/topics")
  public ResponseEntity getMethodName(@RequestParam UUID topicId) {
    Topic topic = topicRepository.findById(topicId)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Topic not found" + topicId));

    TopicResponse formattedTopic = topicMapper.toResponse(topic);
    
    return ResponseEntity
    .status(HttpStatus.OK)
    .body(new ApiResponse<>(
          200,
          "Topic Retrieved",
          formattedTopic
      ));
  }
}
