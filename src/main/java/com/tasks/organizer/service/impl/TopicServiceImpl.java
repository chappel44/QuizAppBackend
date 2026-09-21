package com.tasks.organizer.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.tasks.organizer.dto.request.AnswerDTO;
import com.tasks.organizer.dto.request.CreateTopicDTO;
import com.tasks.organizer.dto.request.QuestionDTO;
import com.tasks.organizer.dto.request.TopicDTO;
import com.tasks.organizer.entities.Answer;
import com.tasks.organizer.entities.Question;
import com.tasks.organizer.entities.Section;
import com.tasks.organizer.entities.Topic;
import com.tasks.organizer.repository.AnswerRepository;
import com.tasks.organizer.repository.QuestionRepository;
import com.tasks.organizer.repository.SectionRepository;
import com.tasks.organizer.repository.TopicRepository;
import com.tasks.organizer.service.TopicService;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

@Service 
@AllArgsConstructor 
public class TopicServiceImpl implements TopicService {
  final TopicRepository topicRepository;
  final SectionRepository sectionRepository;
  final QuestionRepository questionRepository;
  final AnswerRepository answerRepository;

  public record ApiResponse<T>(
    int status,
    String message,
    T data
  ) {}

  final Integer answerLimit = 5;

  @Transactional
  private Question createQuestion(QuestionDTO question, Topic topic) {
      Question newQuestion = Question.builder()
      .question(question.getQuestion())
      .answer(question.getAnswer())
      .points(question.getPoints())
      .imageUrl(question.getImageUrl())
      .topic(topic).build();

    List<Answer> newAnswers = new ArrayList<>();
    Boolean trueFound = false;
    Integer count = 0;
    for (AnswerDTO answer: question.getAnswers()){
      Answer newAnswer = Answer.builder()
      .answer(answer.getAnswer())
      .isCorrect(answer.getIsCorrect())
      .question(newQuestion).build();
      newAnswers.add(newAnswer);

      count++;
      if(count > answerLimit){
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Max answer count of " + answerLimit + " exceeded.");
      }
      
      if(newAnswer.getIsCorrect()){
        if(!trueFound)
          trueFound = true;
        else
          throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Duplicate correct answer found. ");
      }
    }
    newQuestion.setAnswers(newAnswers);
    return newQuestion;
  }

  @Transactional 
  public String generateTopic(CreateTopicDTO entity, UUID sectionId){
      Section section = sectionRepository.findById(sectionId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Section not found " + sectionId));

      if(!section.getIsActive()){
        throw new ResponseStatusException(HttpStatus.CONFLICT, "This section is marked inactive " + sectionId);
      }
      TopicDTO topicDTO = entity.getTopic();

      //Construct a new Topic
      Topic newTopic = Topic.builder()
      .section(section)
      .topicType(topicDTO.getTopicType())
      .name(topicDTO.getName())
      .description(topicDTO.getDescription())
      .questionPoolSize(topicDTO.getQuestionPoolSize())
      .dueDate(topicDTO.getDueDate()).build();

      //Save topic to the repository
      Topic insertedTopic = topicRepository.save(newTopic);

      List<QuestionDTO> requestQuestions = new ArrayList<>(entity.getQuestions());
      List<Question> questionsToInsert = new ArrayList<>(); 
      for (QuestionDTO question: requestQuestions){
        Question newQuestion = createQuestion(question, insertedTopic);

        questionsToInsert.add(newQuestion);
      }

      //Save questions
      questionRepository.saveAll(questionsToInsert);

      return "Submitted successfully";
  }

  @Transactional 
  public ResponseEntity updateTopic(UUID topicId, CreateTopicDTO request) {
    Topic prevTopic = topicRepository.findById(topicId)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Topic not found" + topicId));
    
    TopicDTO requestTopic = request.getTopic();
    
    //Update name if changed
    if(!prevTopic.getName().equals(requestTopic.getName())){
      prevTopic.setName(requestTopic.getName());
    }

    //Update description if changed
    if(!prevTopic.getDescription().equals(requestTopic.getDescription())){
      prevTopic.setDescription(requestTopic.getDescription());
    }

    //Update whether the topic is a test, quiz or review
    if(prevTopic.getTopicType() != requestTopic.getTopicType()){
      prevTopic.setTopicType(requestTopic.getTopicType());
    }

    //Update which section a topic is associated with
    if(!prevTopic.getSection().getId().equals(requestTopic.getSectionId())){

      Section updatedSection = sectionRepository.findById(requestTopic.getSectionId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Section not found" + topicId));
      
      prevTopic.setSection(updatedSection);
    }

    //Change the topic being active or inactive
    if(!requestTopic.getIsActive().equals(prevTopic.getIsActive()))
    {
      prevTopic.setIsActive(requestTopic.getIsActive());
    }

    //Update the due date
    if(!requestTopic.getDueDate().equals(prevTopic.getDueDate()))
    {
      prevTopic.setDueDate(requestTopic.getDueDate());
    }

    if(!requestTopic.getQuestionPoolSize().equals(prevTopic.getQuestionPoolSize())){
      prevTopic.setQuestionPoolSize(requestTopic.getQuestionPoolSize());
    }

    Map<UUID, QuestionDTO> requestQuestions = new HashMap<>();
    List<Question> questionsToAdd = new ArrayList<>();
    List<Question> questionsToRemove  = new ArrayList<>();

    //Find active questions in DB
    List<Question> activeQuestions = questionRepository.findAllByTopic(prevTopic)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Topic not found" + prevTopic));

    //Find questions associated with request
    if(request.getQuestions() != null){
      for (QuestionDTO question : request.getQuestions()){
        if(question.getId() != null){ //Only add when there's an id
          requestQuestions.put(question.getId(), question);
        }
        else{ //No id present, we must add the question
          Question newQuestion = createQuestion(question, prevTopic);
          questionsToAdd.add(newQuestion);
        }
      }
    }

    List<Answer> answersToAdd = new ArrayList<>();
    for(Question prevQuestion : activeQuestions){
      if(!requestQuestions.containsKey(prevQuestion.getId())) { //Id in request questions not found, add to deleted
        questionsToRemove.add(prevQuestion);
      }
      else { //Check for question modifications
        QuestionDTO requestQuestion = requestQuestions.get(prevQuestion.getId());

        if (!requestQuestion.getQuestion().equals(prevQuestion.getQuestion())){
          prevQuestion.setQuestion(requestQuestion.getQuestion());
        }
        if(!requestQuestion.getAnswer().equals(prevQuestion.getAnswer())){
          prevQuestion.setAnswer(requestQuestion.getAnswer());
        }
        if(requestQuestion.getPoints() != prevQuestion.getPoints()){
          prevQuestion.setPoints(requestQuestion.getPoints());
        }
        if(!requestQuestion.getImageUrl().equals(prevQuestion.getImageUrl())){
          prevQuestion.setImageUrl(requestQuestion.getImageUrl());
        }

        Map<UUID, AnswerDTO> requestAnswers = new HashMap<>();

        //Find answers associated with the question
        if(requestQuestion.getAnswer() != null) {
          Boolean trueFound = false;
          Integer count = 0;
          for(AnswerDTO answer : requestQuestion.getAnswers()) {
            if(answer.getIsCorrect()){ //Ensure no duplicate correct answers are inserted
              if (trueFound) {
                
                return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse<>(
                    400,
                    "Duplicate correct answer found on question ",
                    answer
                ));
              }
              
              trueFound=true;
            }

            if(answer.getId() != null){
              requestAnswers.put(answer.getId(), answer);
            }
            else{
              Answer newAnswer = Answer.builder()
              .question(prevQuestion)
              .answer(answer.getAnswer())
              .isCorrect(answer.getIsCorrect()).build();

              answersToAdd.add(newAnswer);
            }
            count++;
            if(count > answerLimit){
              return ResponseEntity
              .status(HttpStatus.BAD_REQUEST)
              .body(new ApiResponse<>(
                  400,
                  "Max answer count of " + answerLimit + " exceeded.",
                  prevQuestion.getId()
              ));
            }
          }
        }

        List<Answer> answersToSet = new ArrayList<>();
        for(Answer answer: prevQuestion.getAnswers()){
          if(requestAnswers.containsKey(answer.getId())){
            AnswerDTO requestAnswer = requestAnswers.get(answer.getId());

            if(!requestAnswer.getAnswer().equals(answer.getAnswer())){
              answer.setAnswer(requestAnswer.getAnswer());
            }
            if(!requestAnswer.getIsCorrect().equals(answer.getIsCorrect())){
              answer.setIsCorrect(requestAnswer.getIsCorrect());
            }
            answersToSet.add(answer);
          }
        }
        prevQuestion.getAnswers().clear();
        prevQuestion.getAnswers().addAll(answersToSet);
      }
    }

    questionRepository.deleteAll(questionsToRemove);
    answerRepository.saveAll(answersToAdd);
    questionRepository.saveAll(questionsToAdd);
      
    return ResponseEntity
      .status(HttpStatus.OK)
      .body(new ApiResponse<>(
            200,
            "Successfully updated topic",
            null 
        ));
  }
}
