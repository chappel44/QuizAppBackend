package com.tasks.organizer.attempt;

import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tasks.organizer.common.dto.ApiResponse;
import com.tasks.organizer.question.mapper.QuestionMapper;
import com.tasks.organizer.ratelimit.KeyType;
import com.tasks.organizer.ratelimit.RateLimit;
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

  @RateLimit(capacity = 100, period = 1, unit = ChronoUnit.HOURS, keyType = KeyType.USER_ORGANIZATION_ID)
  @PostMapping("/attempt")
  public ResponseEntity<ApiResponse<Object>> createAttempt(@RequestParam(required = true) UUID topicId) {
    return studentService.createAttempt(topicId);
  }

  @RateLimit(capacity = 300, period = 30, unit = ChronoUnit.MINUTES, keyType = KeyType.USER_ORGANIZATION_ID)
  @PatchMapping("/attempt/record/question")
  public ResponseEntity<ApiResponse<Object>> recordAttemptQuestion(@RequestParam(required = true) UUID answerId, @RequestParam (required = true) UUID attemptQuestionId) {
    return studentService.recordAttemptQuestion(answerId, attemptQuestionId);
  }

  @RateLimit(capacity = 100, period = 1, unit = ChronoUnit.HOURS, keyType = KeyType.USER_ORGANIZATION_ID)
  @PatchMapping("/attempt/test/grade")
  public ResponseEntity<ApiResponse<Object>> gradeAttempt(@RequestParam (required = true) UUID attemptId) {
    return studentService.gradeAttempt(attemptId);
  }

  @RateLimit(capacity = 300, period = 1, unit = ChronoUnit.HOURS, keyType = KeyType.USER_ORGANIZATION_ID)
  @GetMapping("/attempt")
  public ResponseEntity<ApiResponse<Object>> getAttempt(@RequestParam (required = true) UUID attemptId) {
    return studentService.getAttempt(attemptId);
  }

  @RateLimit(capacity = 300, period = 1, unit = ChronoUnit.HOURS, keyType = KeyType.USER_ORGANIZATION_ID)
  @GetMapping("/attempts")
  public ResponseEntity<ApiResponse<Object>> getAttemptsWithTopic(@RequestParam (required = true) UUID topicId, @RequestParam (required = false) UUID userId) {
    return studentService.getTopicAndAttempts(topicId, userId);
  }
}
