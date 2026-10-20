package com.tasks.organizer.attempt;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tasks.organizer.common.dto.ApiResponse;
import com.tasks.organizer.question.mapper.QuestionMapper;
import com.tasks.organizer.attempt.service.AttemptService;
import com.tasks.organizer.authentication.service.JwtService;
import com.tasks.organizer.topic.TopicRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;


@RequiredArgsConstructor 
@RestController
@RequestMapping ("/api/authenticated")
public class AttemptController {
  final TopicRepository topicRepository;
  final QuestionMapper questionMapper;
  final JwtService jwtService;
  final AttemptService studentService;

  @PostMapping("/attempt")
  public ResponseEntity<ApiResponse<Object>> createAttempt(@RequestParam(required = true) UUID topicId) {
    return studentService.createAttempt(topicId);
  }

  @PatchMapping("/attempt/record/question")
  public ResponseEntity<ApiResponse<Object>> recordAttemptQuestion(@RequestParam(required = true) UUID answerId, @RequestParam (required = true) UUID attemptQuestionId) {
    return studentService.recordAttemptQuestion(answerId, attemptQuestionId);
  }

  @PatchMapping("/attempt/test/grade")
  public ResponseEntity<ApiResponse<Object>> gradeAttempt(@RequestParam (required = true) UUID attemptId) {
    return studentService.gradeAttempt(attemptId);
  }

  @GetMapping("/attempt")
  public ResponseEntity<ApiResponse<Object>> getAttempt(@RequestParam (required = true) UUID attemptId, @RequestParam (required = false) UUID userId) {
    return studentService.getAttempt(attemptId, userId);
  }

  @GetMapping("/attempts")
  public ResponseEntity<ApiResponse<Object>> getAttemptsWithTopic(@RequestParam (required = true) UUID topicId, @RequestParam (required = false) UUID userId) {
    return studentService.getTopicAndAttempts(topicId, userId);
  }
}
