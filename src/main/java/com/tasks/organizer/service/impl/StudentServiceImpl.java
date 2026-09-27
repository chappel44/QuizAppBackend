package com.tasks.organizer.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.tasks.organizer.controller.AdminController.ApiResponse;
import com.tasks.organizer.dto.response.AttemptResponse;
import com.tasks.organizer.dto.response.AttemptsWithTopicResponse;
import com.tasks.organizer.dto.response.GetAttemptResponse;
import com.tasks.organizer.dto.response.TopicResponse;
import com.tasks.organizer.entities.Answer;
import com.tasks.organizer.entities.Attempt;
import com.tasks.organizer.entities.AttemptQuestion;
import com.tasks.organizer.entities.Question;
import com.tasks.organizer.entities.Role;
import com.tasks.organizer.entities.Topic;
import com.tasks.organizer.entities.User;
import com.tasks.organizer.entities.Topic.TopicType;
import com.tasks.organizer.mappers.AttemptMapper;
import com.tasks.organizer.mappers.TopicMapper;
import com.tasks.organizer.repository.AttemptQuestionRepository;
import com.tasks.organizer.repository.AttemptRepository;
import com.tasks.organizer.repository.QuestionRepository;
import com.tasks.organizer.repository.ResultRepository;
import com.tasks.organizer.repository.TopicRepository;
import com.tasks.organizer.service.StudentService;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

@Service 
@AllArgsConstructor 
public class StudentServiceImpl implements StudentService{
  final TopicRepository topicRepository;
  final AttemptRepository attemptRepository;
  final AttemptQuestionRepository attemptQuestionRepository;
  final QuestionRepository questionRepository;
  final ResultRepository resultRepository;
  final TopicMapper topicMapper;
  final AttemptMapper attemptMapper;

  @Transactional
  public ResponseEntity<ApiResponse<?>> createAttempt(UUID topicId) {
    Optional<Topic> topicOpt = topicRepository.findById(topicId);


    if (topicOpt.isEmpty()) {
      return ResponseEntity
      .status(HttpStatus.NOT_FOUND)
      .body(new ApiResponse<>(404, "Topic not found", null));
    }

    Topic topic = topicOpt.get();

    // Start with only active questions
    List<Question> activeQuestions = topic.getQuestions().stream()
    .filter(Question::getIsActive)
    .collect(Collectors.toList());

    // Narrow down to a random subset if this topic type calls for it
    List<Question> selectedQuestions;
    if (topic.getTopicType().equals(TopicType.RANDOM_QUESTIONS)) {
      selectedQuestions = new ArrayList<>(activeQuestions);

      while (selectedQuestions.size() > topic.getQuestionPoolSize()) {
        int randomIndex = ThreadLocalRandom.current().nextInt(0, selectedQuestions.size());
        selectedQuestions.remove(randomIndex);
      }
    } 
    else {
      selectedQuestions = activeQuestions;
    }

    double totalPoints = 0;
    for (Question question : selectedQuestions) {
      totalPoints += question.getPoints();
    }

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    User user = (User) authentication.getPrincipal();

    Attempt newAttempt = Attempt.builder()
    .topic(topic)
    .user(user)
    .pointsEarned(0)
    .totalPoints(totalPoints)
    .percentage(0)
    .build();

    List<AttemptQuestion> attemptQuestions = new ArrayList<>();
    for (Question question : selectedQuestions) {
      AttemptQuestion attemptQuestion = AttemptQuestion.builder()
      .attempt(newAttempt)
      .isCorrect(null)
      .question(question)
      .build();
      attemptQuestions.add(attemptQuestion);
    }

    newAttempt.setAttemptQuestions(attemptQuestions);

    Attempt insertedAttempt = attemptRepository.save(newAttempt);

    return ResponseEntity
    .status(HttpStatus.OK)
    .body(new ApiResponse<>(200, "Successfully created attempt", insertedAttempt.getId()));
  }

  @Transactional
  public ResponseEntity recordAttemptQuestion(UUID answerId, UUID attemptQuestionId){
    Optional<AttemptQuestion> attemptQuestionOpt = attemptQuestionRepository.findById(attemptQuestionId);
    
    if(!attemptQuestionOpt.isPresent()){
      return ResponseEntity
      .status(HttpStatus.NOT_FOUND)
      .body(new ApiResponse<>(
        404,
        "Attempt question not found",
        attemptQuestionId
      ));
    }

    AttemptQuestion attemptQuestion = attemptQuestionOpt.get();

    Attempt attempt = attemptQuestion.getAttempt();

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    User user = (User) authentication.getPrincipal();

    if(!attempt.getUser().getId().equals(user.getId())){
      return ResponseEntity
      .status(HttpStatus.UNAUTHORIZED)
      .body(new ApiResponse<>(
        403,
        "Unauthorized",
        null
      ));
    }

    if(attempt.isFinalized()){
      return ResponseEntity
      .status(HttpStatus.CONFLICT )
      .body(new ApiResponse<>(
        409,
        "This attempts grade has been finalized",
        attempt.getId()
      ));
    }

    Boolean isCorrect = attemptQuestion.getIsCorrect();
    if(Boolean.TRUE.equals(isCorrect)){ 
      return ResponseEntity
      .status(HttpStatus.CONFLICT)
      .body(new ApiResponse<>(
        409,
        "Question already graded as correct",
        attemptQuestionId
      ));
    }

    Question question = attemptQuestion.getQuestion();
    List<Answer> answers = question.getAnswers();

    double pointEarned = 0;
    boolean answerFound = false; //Keep track of if the attempt id in the answer set
    boolean answerCorrect = false; //Keep track of whether the answer is correct or not.
    
    for(Answer answer: answers){
      if(answer.getId().equals(answerId)){
        answerFound = true;
        if(answer.getIsCorrect()){
          pointEarned += question.getPoints();
          answerCorrect = true;
        }
      }
    }

    if(!answerFound){
      return ResponseEntity
      .status(HttpStatus.NOT_FOUND)
      .body(new ApiResponse<>(
        404,
        "Answer provided doesnt exist in answer set.",
        answerId
      ));
    }

    attemptQuestion.setSubmittedAnswerId(answerId);

    if(attempt.getTopic().getTopicType() != TopicType.TEST) { // Only grade question when the topic is not a test
      double totalPointsEarned = attempt.getPointsEarned() + pointEarned;
      double totalPoints = attempt.getTotalPoints();

      attempt.setPointsEarned(totalPointsEarned);
      attempt.setPercentage(totalPointsEarned / totalPoints);
      attemptQuestion.setIsCorrect(answerCorrect);
    }
    else{ //For tests dont return whether the answer is correct or not, just record answer id
      return ResponseEntity
      .status(HttpStatus.OK)
      .body(new ApiResponse<>(
        200,
        "Answer successfully recorded",
        null
      ));
    }
    
    return ResponseEntity
    .status(HttpStatus.OK)
    .body(new ApiResponse<>(
      200,
      "Answer successfully recorded",
      answerCorrect
    ));
  }

  @Transactional 
  public ResponseEntity gradeAttempt(UUID attemptId) {
    Optional<Attempt> attemptOpt = attemptRepository.findById(attemptId);

    if(!attemptOpt.isPresent()){
      return ResponseEntity
      .status(HttpStatus.NOT_FOUND)
      .body(new ApiResponse<>(
        404,
        "Attempt could not be found",
        null
      ));
    }

    Attempt attempt = attemptOpt.get();
    
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    User user = (User) authentication.getPrincipal();

    if(!attempt.getUser().getId().equals(user.getId())) {
      return ResponseEntity
      .status(HttpStatus.UNAUTHORIZED)
      .body(new ApiResponse<>(
        403,
        "Unauthorized",
        null
      ));
    }

    if(attempt.isFinalized()) {
      return ResponseEntity
      .status(HttpStatus.CONFLICT)
      .body(new ApiResponse<>(
        409,
        "This attempt has already been graded",
        null
      ));
    }

    Optional<Topic> attemptTopicOpt = topicRepository.findById(attempt.getTopic().getId()); 
    
    if(!attemptTopicOpt.isPresent()){
      return ResponseEntity
      .status(HttpStatus.NOT_FOUND)
      .body(new ApiResponse<>(
        404,
        "Topic associated with attemp not found",
        null
      ));
    }

    Topic attemptTopic = attemptTopicOpt.get();


    List<Question> attemptTopicQuestions = questionRepository.findByTopicIdWithAnswers(attempt.getTopic().getId());
    
    if(!attemptTopic.getTopicType().equals(TopicType.TEST)){
      return ResponseEntity
      .status(HttpStatus.CONFLICT)
      .body(new ApiResponse<>(
        409,
        "This attempt is not a tested event",
        null
      ));
    }

    List<AttemptQuestion> attemptQuestions = attempt.getAttemptQuestions();
    Map<UUID, AttemptQuestion> attemptAnswerIds = new HashMap<>();

    for (AttemptQuestion attemptQuestion : attemptQuestions) {
      attemptAnswerIds.put(attemptQuestion.getQuestion().getId(), attemptQuestion);
    }

    double pointsEarned = 0;

    for (Question question : attemptTopicQuestions) {
      AttemptQuestion matchedAttemptQuestion = attemptAnswerIds.get(question.getId());

      if (matchedAttemptQuestion == null) {
        continue; 
      }

      UUID submittedAnswerId = matchedAttemptQuestion.getSubmittedAnswerId();

      if (submittedAnswerId == null) {
        // student never answered this question — mark wrong, no points
        matchedAttemptQuestion.setIsCorrect(false);
        continue;
      }

      boolean isCorrect = false;
      for (Answer answer : question.getAnswers()) {
        if (answer.getId().equals(submittedAnswerId) && Boolean.TRUE.equals(answer.getIsCorrect())) {
          isCorrect = true;
          break;
        }
      }

      matchedAttemptQuestion.setIsCorrect(isCorrect);
      if (isCorrect) {
        pointsEarned += question.getPoints();
      }
    }
    double percentage = pointsEarned / attempt.getTotalPoints();

    attempt.setPercentage(percentage);
    attempt.setPointsEarned(pointsEarned);
    attempt.setFinalized(true);

    return ResponseEntity
    .status(HttpStatus.OK)
    .body(new ApiResponse<>(
      200,
      "Test grade submitted",
      null
    ));    
  }

  /*
  Query params: attemptId
  Expected Response:
  {
    Shape returned
    attempt: {
      id,
      total_points,
      points_earned,
      attempt_questions: [
        {
          id,
          is_correct,
          submitted_answer_id
        }
      ]
    },
    topics: {
      id,
      name,
      description,
      topic_type,
      questions: [
        {
          id,
          question,
          points,
          image_url,
          answers: [
            {
              id,
              answer
            }
          ]
        }
      ]
    }
  }
  */

  public ResponseEntity getAttempt(UUID attemptId){
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    User user = (User) authentication.getPrincipal();

    Optional<Attempt> attemptOpt = attemptRepository.findByTopicIdWithAnswers(attemptId);

    if (attemptOpt.isEmpty()) {
      System.out.println("No attempt found for id: " + attemptId);
      return ResponseEntity
      .status(HttpStatus.NOT_FOUND)
      .body(new ApiResponse<>(404, "Attempt not found", null));
    }

    Attempt attempt = attemptOpt.get();

    if(!attempt.getUser().getId().equals(user.getId()) && user.getRole() != Role.ADMIN){
      return ResponseEntity
      .status(HttpStatus.UNAUTHORIZED)
      .body(new ApiResponse<>(
        403,
        "Unauthorized",
        null
      ));
    }

    Topic topic = attempt.getTopic();

    List<UUID> attemptQuestionIds = new ArrayList<>();

    System.out.println("=== AttemptQuestions ===");
    for (AttemptQuestion aq : attempt.getAttemptQuestions()) {
      attemptQuestionIds.add(aq.getQuestion().getId());
    }

    TopicResponse formattedTopic = TopicResponse.builder().id(topic.getId())
    .createdAt(topic.getCreatedAt())
    .name(topic.getName())
    .description(topic.getName())
    .isActive(topic.getIsActive())
    .dueDate(topic.getDueDate())
    .topicType(topic.getTopicType())
    .build();

    AttemptResponse attemptResponse = attemptMapper.toResponse(attempt);

    GetAttemptResponse getAttemptResponse = GetAttemptResponse.builder()
    .attempt(attemptResponse)
    .topic(formattedTopic).build();

    return ResponseEntity
    .status(HttpStatus.OK)
    .body(new ApiResponse<>(
      200,
      "Attempt retrieval successful",
      getAttemptResponse
    ));
  }

  public ResponseEntity getTopicAndAttempts(UUID topicId){
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    User user = (User) authentication.getPrincipal();

    Optional<Topic> topicOpt = topicRepository.findById(topicId);

    if(topicOpt.isEmpty()){
      return ResponseEntity
      .status(HttpStatus.NOT_FOUND)
      .body(new ApiResponse<>(404, "Topic not found", null));
    }

    Topic topic = topicOpt.get();
    
    if(topic.getIsActive().equals(false)){
      return ResponseEntity
      .status(HttpStatus.CONFLICT)
      .body(new ApiResponse<>(409, "Topic is inactive", null));
    }

    Optional<List<Attempt>> currentAttemptsOpt = attemptRepository.findByUserIdAndTopicId(user.getId(), topic.getId());

    if(currentAttemptsOpt.isEmpty()){
      return ResponseEntity
      .status(HttpStatus.NOT_FOUND)
      .body(new ApiResponse<>(404, "Attempts with topic not found", null)); 
    }

    List<Attempt> currentAttempts = currentAttemptsOpt.get();
    List<AttemptResponse> attemptResponses = new ArrayList<>();
    for (Attempt att: currentAttempts){
      AttemptResponse attResponse = AttemptResponse.builder()
      .id(att.getId())
      .percentage(att.getPercentage())
      .totalPoints(att.getTotalPoints())
      .pointsEarned(att.getPointsEarned()).build();

      attemptResponses.add(attResponse);
    }

    TopicResponse topicResponse = topicMapper.toResponse(topic);
    topicResponse.setQuestions(null);

    AttemptsWithTopicResponse attemptsWithTopicResponse = new AttemptsWithTopicResponse();

    attemptsWithTopicResponse.setAttempts(attemptResponses);
    attemptsWithTopicResponse.setTopic(topicResponse);

    return ResponseEntity
    .status(HttpStatus.OK)
    .body(new ApiResponse<>(
      200,
      "Attempts and topic successfully retrieved",
      attemptsWithTopicResponse
    ));
  }
}
