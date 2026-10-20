package com.tasks.organizer.attempt.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.tasks.organizer.common.dto.ApiResponse;
import com.tasks.organizer.attempt.ResultRepository;
import com.tasks.organizer.attempt.dto.response.AttemptResponse;
import com.tasks.organizer.attempt.dto.response.AttemptsWithTopicResponse;
import com.tasks.organizer.attempt.dto.response.GetAttemptResponse;
import com.tasks.organizer.topic.dto.response.TopicResponse;
import com.tasks.organizer.question.entity.Answer;
import com.tasks.organizer.attempt.entity.Attempt;
import com.tasks.organizer.attempt.entity.AttemptQuestion;
import com.tasks.organizer.question.entity.Question;
import com.tasks.organizer.user.entity.Role;
import com.tasks.organizer.topic.entity.Topic;
import com.tasks.organizer.user.entity.User;
import com.tasks.organizer.topic.entity.Topic.TopicType;
import com.tasks.organizer.attempt.mapper.AttemptMapper;
import com.tasks.organizer.attempt.repository.AttemptQuestionRepository;
import com.tasks.organizer.attempt.repository.AttemptRepository;
import com.tasks.organizer.topic.mapper.TopicMapper;
import com.tasks.organizer.question.QuestionRepository;
import com.tasks.organizer.topic.TopicRepository;

import org.springframework.transaction.annotation.Transactional;

import lombok.AllArgsConstructor;

@Service 
@AllArgsConstructor  
public class AttemptServiceImpl implements AttemptService{
  final TopicRepository topicRepository;
  final AttemptRepository attemptRepository;
  final AttemptQuestionRepository attemptQuestionRepository;
  final QuestionRepository questionRepository;
  final ResultRepository resultRepository;
  final TopicMapper topicMapper;
  final AttemptMapper attemptMapper;

  @Transactional
  public ResponseEntity<ApiResponse<Object>> createAttempt(UUID topicId) {
    Optional<Topic> topicOpt = topicRepository.findById(topicId);

    if (topicOpt.isEmpty()) {
      return ApiResponse.error(HttpStatus.NOT_FOUND, "Topic not found");
    }

    Topic topic = topicOpt.get();

    if(topic.getIsActive().equals(false)){
      return ApiResponse.error(HttpStatus.NOT_FOUND, "Topic is inactive");
    }

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

    // newAttempt.setAttemptQuestions(attemptQuestions);

    Attempt insertedAttempt = attemptRepository.save(newAttempt);
    attemptQuestionRepository.saveAll(attemptQuestions);

    return ApiResponse.ok("Successfully created attempt", insertedAttempt.getId());
  }

  @Transactional
  public ResponseEntity<ApiResponse<Object>> recordAttemptQuestion(UUID answerId, UUID attemptQuestionId){
    Optional<AttemptQuestion> attemptQuestionOpt = attemptQuestionRepository.findById(attemptQuestionId);
    
    if(!attemptQuestionOpt.isPresent()){
      return ApiResponse.of(HttpStatus.NOT_FOUND, "Attempt question not found", attemptQuestionId);
    }

    AttemptQuestion attemptQuestion = attemptQuestionOpt.get();

    Attempt attempt = attemptQuestion.getAttempt(); //Performs a SELECT * FROM AttemptQuestion aq LEFT JOIN Attempt a ON aq.attempt_id = a.id 

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    User user = (User) authentication.getPrincipal();

    if(!attempt.getUser().getId().equals(user.getId())){
      return ApiResponse.of(HttpStatus.NOT_FOUND, "Attempt question not found", attemptQuestionId);
    }

    if(attempt.isFinalized()){
      return ApiResponse.of(HttpStatus.CONFLICT, "This attempts grade has been finalized", attempt.getId());
    }

    Boolean isCorrect = attemptQuestion.getIsCorrect();
    if(Boolean.TRUE.equals(isCorrect)){ 
      return ApiResponse.of(HttpStatus.CONFLICT, "Question already graded as correct", attemptQuestionId);
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
      return ApiResponse.of(HttpStatus.NOT_FOUND, "Answer provided doesnt exist in answer set.", answerId);
    }

    attemptQuestion.setSubmittedAnswerId(answerId);

    if(attempt.getTopic().getTopicType() != TopicType.TEST) { // Only grade question when the topic is not a test
      double totalPointsEarned = attempt.getPointsEarned() + pointEarned;
      double totalPoints = attempt.getTotalPoints();

      attempt.setPointsEarned(totalPointsEarned);

      //Treat an attempt with no points as a 100
      attempt.setPercentage((totalPoints <= 0) ? 1.0 : totalPointsEarned / totalPoints);
      attemptQuestion.setIsCorrect(answerCorrect);
    }
    else{ //For tests dont return whether the answer is correct or not, just record answer id
      return ApiResponse.ok("Answer successfully recorded", null);
    }

    return ApiResponse.ok("Answer successfully recorded", answerCorrect);
  }

  @Transactional 
  public ResponseEntity<ApiResponse<Object>> gradeAttempt(UUID attemptId) {
    Optional<Attempt> attemptOpt = attemptRepository.findById(attemptId);

    if(!attemptOpt.isPresent()){
      return ApiResponse.error(HttpStatus.NOT_FOUND, "Attempt not found");
    }

    Attempt attempt = attemptOpt.get();
    
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    User user = (User) authentication.getPrincipal();

    if(!attempt.getUser().getId().equals(user.getId())) {
      return ApiResponse.error(HttpStatus.NOT_FOUND, "Attempt not found");
    }

    if(attempt.isFinalized()) {
      return ApiResponse.error(HttpStatus.CONFLICT, "This attempt has already been graded");
    }

    Optional<Topic> attemptTopicOpt = topicRepository.findById(attempt.getTopic().getId()); 
    
    if(!attemptTopicOpt.isPresent()){
      return ApiResponse.error(HttpStatus.NOT_FOUND, "Topic associated with attempt not found");
    }

    Topic attemptTopic = attemptTopicOpt.get();
    
    List<Question> attemptTopicQuestions = questionRepository.findByTopicIdWithAnswers(attempt.getTopic().getId());
    
    if(!attemptTopic.getTopicType().equals(TopicType.TEST)){
      return ApiResponse.error(HttpStatus.CONFLICT, "This attempt is not a tested event");
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

    return ApiResponse.ok("Test grade submitted", null);    
  }

  @Transactional (readOnly = true) //This is used to prevent calling setQuestion() and it override the topic questions
  public ResponseEntity<ApiResponse<Object>> getAttempt(UUID attemptId){
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    User user = (User) authentication.getPrincipal();

    Optional<Attempt> attemptOpt = attemptRepository.findByTopicIdWithAnswers(attemptId);

    if (attemptOpt.isEmpty()) {
      return ApiResponse.error(HttpStatus.NOT_FOUND, "Attempt not found");
    }

    Attempt attempt = attemptOpt.get();

    if(!attempt.getUser().getId().equals(user.getId()) && user.getRole() != Role.SITE_ADMIN){
      return ApiResponse.error(HttpStatus.NOT_FOUND, "Attempt not found");
    }

    Topic topic = attempt.getTopic();
    List question = questionRepository.findByTopicIdWithAnswers(topic.getId());
    topic.setQuestions(question);

    TopicResponse formattedTopic = TopicResponse.builder()
      .id(topic.getId())
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
      .topic(formattedTopic)
    .build();

    return ApiResponse.ok("Attempt retrieval successful", getAttemptResponse);
  }

  public ResponseEntity<ApiResponse<Object>> getTopicAndAttempts(UUID topicId){
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    User user = (User) authentication.getPrincipal();

    Optional<Topic> topicOpt = topicRepository.findById(topicId);

    if(topicOpt.isEmpty()){
      return ApiResponse.error(HttpStatus.NOT_FOUND, "Topic not found");
    }

    Topic topic = topicOpt.get();
    
    if(topic.getIsActive().equals(false)){
      return ApiResponse.error(HttpStatus.CONFLICT, "Topic is inactive");
    }

    Optional<List<Attempt>> currentAttemptsOpt = attemptRepository.findByUserIdAndTopicId(user.getId(), topic.getId());

    if(currentAttemptsOpt.isEmpty()){
      return ApiResponse.error(HttpStatus.NOT_FOUND, "Attempts with topic not found"); 
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

    // TopicResponse topicResponse = topicMapper.toResponse(topic);
    // topicResponse.setQuestions(null);

    TopicResponse topicResponse = TopicResponse.builder().name(topic.getName()).id(topic.getId()).topicType(topic.getTopicType()).build();

    AttemptsWithTopicResponse attemptsWithTopicResponse = new AttemptsWithTopicResponse();

    attemptsWithTopicResponse.setAttempts(attemptResponses);
    attemptsWithTopicResponse.setTopic(topicResponse);

    return ApiResponse.ok("Attempts and topic successfully retrieved", attemptsWithTopicResponse);
  }
}
