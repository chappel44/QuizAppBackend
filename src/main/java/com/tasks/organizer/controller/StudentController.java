package com.tasks.organizer.controller;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tasks.organizer.mappers.QuestionMapper;
import com.tasks.organizer.mappers.ResultMapper;
import com.tasks.organizer.repository.ResultRepository;
import com.tasks.organizer.repository.TopicRepository;
import com.tasks.organizer.service.JwtService;
import com.tasks.organizer.service.StudentService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;


@RequiredArgsConstructor 
@RestController
@RequestMapping ("/api/student")
public class StudentController {
  final TopicRepository topicRepository;
  final ResultRepository resultRepository;
  final QuestionMapper questionMapper;
  final JwtService jwtService;
  final ResultMapper resultMapper;
  final StudentService studentService;

  @PostMapping("/attempt")
  public ResponseEntity createAttempt(@RequestParam(required = true) UUID topicId) {
    return studentService.createAttempt(topicId);
  }

  @PatchMapping("/attempt/record/question")
  public ResponseEntity recordAttemptQuestion(@RequestParam(required = true) UUID answerId, @RequestParam (required = true) UUID attemptQuestionId) {
    return studentService.recordAttemptQuestion(answerId, attemptQuestionId);
  }

  @PatchMapping("/attempt/test/grade")
  public ResponseEntity gradeAttempt(@RequestParam (required = true) UUID attemptId) {
    return studentService.gradeAttempt(attemptId);
  }

  @GetMapping("/attempt")
  public ResponseEntity getAttempt(@RequestParam (required = true) UUID attemptId) {
    return studentService.getAttempt(attemptId);
  }

  @GetMapping("/attempts")
  public ResponseEntity getAttemptsWithTopic(@RequestParam (required = true) UUID topicId) {
    return studentService.getTopicAndAttempts(topicId);
  }
}
