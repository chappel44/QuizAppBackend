package com.tasks.organizer.controller;

import com.tasks.organizer.repository.QuestionRepository;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.tasks.organizer.controller.AdminController.ApiResponse;
import com.tasks.organizer.dto.request.GradeTopicDTO;
import com.tasks.organizer.dto.response.IdResponse;
import com.tasks.organizer.dto.response.ResultsResponse;
import com.tasks.organizer.entities.Answer;
import com.tasks.organizer.entities.Question;
import com.tasks.organizer.entities.Results;
import com.tasks.organizer.entities.Topic;
import com.tasks.organizer.entities.User;
import com.tasks.organizer.entities.Topic.TopicType;
import com.tasks.organizer.mappers.QuestionMapper;
import com.tasks.organizer.mappers.ResultMapper;
import com.tasks.organizer.repository.ResultRepository;
import com.tasks.organizer.repository.TopicRepository;
import com.tasks.organizer.service.JwtService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;


@RequiredArgsConstructor 
@RestController
@RequestMapping ("/api/student")
public class StudentController {
  private final QuestionRepository questionRepository;
  final TopicRepository topicRepository;
  final ResultRepository resultRepository;
  final QuestionMapper questionMapper;
  final JwtService jwtService;
  final ResultMapper resultMapper;

  @PostMapping("/topics/grade-test")
  public ResponseEntity gradeTopic(@Validated 
    @RequestBody GradeTopicDTO request, 
    @RequestParam UUID topicId) {
    System.out.println("POST HIT");

    if(topicId == null){
      new ResponseStatusException(HttpStatus.BAD_REQUEST, "Missing topic id" + topicId);
      return ResponseEntity
      .status(HttpStatus.BAD_REQUEST)
      .body(new ApiResponse<>(
            400,
            "Missing topic id",
            null 
        ));
    }

    Topic topic = topicRepository.findById(topicId)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Topic not found" + topicId));

    if(topic.getTopicType() != TopicType.TEST){
      return ResponseEntity
      .status(HttpStatus.CONFLICT)
      .body(new ApiResponse<>(
            400,
            "The topic provided is not a test.",
            topicId 
        ));
    }
    if(!topic.getIsActive()){
      return ResponseEntity
      .status(HttpStatus.CONFLICT)
      .body(new ApiResponse<>(
            400,
            "Topic is inactive",
            topicId 
        ));
    }

    List<Question> questions = topic.getQuestions();
    Set<UUID> correctAnswerSet = new HashSet<UUID>();

    for(UUID answerId: request.getAnswers()){
      correctAnswerSet.add(answerId);
    }

    List<Question> correctQuestions = new ArrayList<>();
    double pointsScored = 0;
    double totalPoints = 0;
    for(Question question: questions){
      totalPoints += question.getPoints();
      for(Answer answer: question.getAnswers()){
        if(correctAnswerSet.contains(answer.getId())){
          correctQuestions.add(question);
          pointsScored += question.getPoints();
          break;
        }
      }
    }

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    User user = (User) authentication.getPrincipal();
    
    double percentageScored = pointsScored / totalPoints;

    Results result = Results.builder()
    .topic(topic)
    .percentage(percentageScored)
    .pointsEarned(pointsScored)
    .totalPoints(totalPoints)
    .user(user)
    .build();

    resultRepository.save(result);

    ResultsResponse newResponse = resultMapper.toResponse(result);
    List<IdResponse> correctIds = questionMapper.toIdResponseList(correctQuestions);
    newResponse.setCorrectIds(correctIds);

    return ResponseEntity
      .status(HttpStatus.OK)
      .body(new ApiResponse<>(
        200,
        "Test submitted",
        newResponse
      ));
    }

    @GetMapping("/question/grade")
    public ResponseEntity getMethodName(@Validated @RequestParam UUID questionId, @RequestParam UUID answerId) {
      Question question = questionRepository.findById(questionId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Could "));

      Topic questionTopic = question.getTopic();

      //Ensure question graded doesn't belong to a test
      if(questionTopic.getTopicType() == TopicType.TEST){
        return ResponseEntity
        .status(HttpStatus.CONFLICT)
        .body(new ApiResponse<>(
          409,
          "Cannot grade this question.",
          null
        ));
      }

      Boolean isCorrect = false;

      for(Answer answer: question.getAnswers()){
        System.out.println("ANSWER ID " + answer.getId() + " ID PASSED IN " + answerId + " IS THIS THE CORRECT ANSWER? " + answer.getIsCorrect());
        
        if(answerId.equals(answer.getId()) && answer.getIsCorrect()){
          isCorrect = true;
        }
      }

      return ResponseEntity
      .status(HttpStatus.OK)
      .body(new ApiResponse<>(
        200,
        "Question graded.",
        isCorrect
      ));
    }
    
  }
