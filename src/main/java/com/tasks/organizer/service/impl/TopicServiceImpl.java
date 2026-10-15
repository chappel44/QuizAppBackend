package com.tasks.organizer.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.Authentication;

import com.tasks.organizer.dto.request.AnswerDTO;
import com.tasks.organizer.dto.request.CreateTopicDTO;
import com.tasks.organizer.dto.request.QuestionDTO;
import com.tasks.organizer.dto.request.TopicDTO;
import com.tasks.organizer.entities.Answer;
import com.tasks.organizer.entities.Question;
import com.tasks.organizer.entities.Role;
import com.tasks.organizer.entities.Section;
import com.tasks.organizer.entities.Topic;
import com.tasks.organizer.entities.User;
import com.tasks.organizer.entities.Topic.TopicType;
import com.tasks.organizer.mappers.TopicMapper;
import com.tasks.organizer.repository.AnswerRepository;
import com.tasks.organizer.repository.QuestionRepository;
import com.tasks.organizer.repository.SectionRepository;
import com.tasks.organizer.repository.TopicRepository;
import com.tasks.organizer.service.TopicService;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Service 
@AllArgsConstructor
public class TopicServiceImpl implements TopicService {
  final TopicRepository topicRepository;
  final SectionRepository sectionRepository;
  final QuestionRepository questionRepository;
  final AnswerRepository answerRepository;
  final TopicMapper topicMapper;

  public record ApiResponse<T>(
    int status,
    String message,
    T data
  ) {}
  
  private Integer answerLimit = 5;

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

      return String.valueOf(insertedTopic.getId());
  }

  @Transactional 
  public ResponseEntity updateTopic(UUID topicId, CreateTopicDTO request) {
    Topic activeTopic = topicRepository.findById(topicId)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Topic not found" + topicId));
    
    TopicDTO requestTopic = request.getTopic();

    //Topic changed if a the topic type, or section it belongs to changes or if the question pool size changes.
    boolean topicChange =
      !requestTopic.getTopicType().equals(activeTopic.getTopicType()) ||
      !Objects.equals(requestTopic.getSectionId(), activeTopic.getSectionId()) ||
      (requestTopic.getQuestionPoolSize() != activeTopic.getQuestionPoolSize()
        && requestTopic.getTopicType() == TopicType.RANDOM_QUESTIONS);    
    
    activeTopic.setName(requestTopic.getName());
    activeTopic.setDescription(requestTopic.getDescription());
    activeTopic.setTopicType(requestTopic.getTopicType());
    
    //Update which section a topic is associated with
    if(!activeTopic.getSectionId().equals(requestTopic.getSectionId())){
      Section updatedSection = sectionRepository.findById(requestTopic.getSectionId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Section not found" + topicId));
      
      activeTopic.setSection(updatedSection);
    }

    activeTopic.setIsActive(requestTopic.getIsActive());
    activeTopic.setDueDate(requestTopic.getDueDate());
    activeTopic.setQuestionPoolSize(requestTopic.getQuestionPoolSize());

    Map<UUID, QuestionDTO> requestQuestions = new HashMap<>();
    List<Question> questionsToAdd = new ArrayList<>();
    List<Question> questionsToRemove  = new ArrayList<>();

    //Find active questions in DB
    List<Question> activeQuestions = questionRepository.findByTopicIdWithAnswers(activeTopic.getId());

    boolean questionChange = 
    activeQuestions.size() != request.getQuestions().size();
    boolean answerChange = false;

    //Find questions associated with request
    if(request.getQuestions() != null){
      for (QuestionDTO question : request.getQuestions()){
        if(question.getId() != null){ //Only add when there's an id
          requestQuestions.put(question.getId(), question);
        }
        else{ //No id present, we must add the question
          questionChange = true;
          Question newQuestion = createQuestion(question, activeTopic);
          questionsToAdd.add(newQuestion);
        }
      }
    }
    
    List<Answer> answersToAdd = new ArrayList<>();

    for(Question activeQuestion : activeQuestions){
      if(!requestQuestions.containsKey(activeQuestion.getId())) { //Id in request questions not found, add to deleted
        questionChange = true;
        questionsToRemove.add(activeQuestion);
      }
      else { //Check for question modifications
        QuestionDTO requestQuestion = requestQuestions.get(activeQuestion.getId());

        //Bump up the version if points differ of the question or active status differ
        questionChange |=
          activeQuestion.getPoints() != requestQuestion.getPoints() || 
          !Objects.equals(activeQuestion.getIsActive(), requestQuestion.isActive()) ||
          activeQuestion.getAnswers().size() != requestQuestion.getAnswers().size();
        
        activeQuestion.setQuestion(requestQuestion.getQuestion());
        activeQuestion.setAnswer(requestQuestion.getAnswer());
        activeQuestion.setPoints(requestQuestion.getPoints());
        activeQuestion.setImageUrl(requestQuestion.getImageUrl());
        activeQuestion.setIsActive(requestQuestion.isActive());

        Map<UUID, AnswerDTO> requestAnswers = new HashMap<>();

        //Find answers associated with the question
        Boolean trueFound = false;
        Integer count = 0;

        for(AnswerDTO requestAnswer : requestQuestion.getAnswers()) {
          if(requestAnswer.getIsCorrect()){ //Ensure no duplicate correct answers are inserted
            if (trueFound) {
              return ResponseEntity
              .status(HttpStatus.BAD_REQUEST)
              .body(new ApiResponse<>(
                400,
                "Duplicate correct answer found on question ",
                requestAnswer
              ));
            }
            
            trueFound=true;
          }
          
          if(requestAnswer.getId() != null){
            requestAnswers.put(requestAnswer.getId(), requestAnswer);
          }
          else{
            answerChange = true;

            Answer newAnswer = Answer.builder()
            .question(activeQuestion)
            .answer(requestAnswer.getAnswer())
            .isCorrect(requestAnswer.getIsCorrect()).build();
            answersToAdd.add(newAnswer);
          }

          count++;
          if(count > answerLimit){
            return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(new ApiResponse<>(
                400,
                "Max answer count of " + answerLimit + " exceeded.",
                activeQuestion.getId()
            ));
          }
        }

        List<Answer> answersToSet = new ArrayList<>();
        for(Answer activeAnswer: activeQuestion.getAnswers()){
          if(requestAnswers.containsKey(activeAnswer.getId())){
            AnswerDTO requestAnswer = requestAnswers.get(activeAnswer.getId());

            if (!Objects.equals(activeAnswer.getIsCorrect(), requestAnswer.getIsCorrect())){
              answerChange = true;
            }

            activeAnswer.setAnswer(requestAnswer.getAnswer());
            activeAnswer.setIsCorrect(requestAnswer.getIsCorrect());

            answersToSet.add(activeAnswer);
          }
        }
        activeQuestion.getAnswers().clear();
        activeQuestion.getAnswers().addAll(answersToSet);
      }
    }

    if(topicChange || questionChange || answerChange){
      activeTopic.setVersion(activeTopic.getVersion() + 1);
    }

    questionRepository.deleteAll(questionsToRemove);
    answerRepository.saveAll(answersToAdd);
    questionRepository.saveAll(questionsToAdd);

    System.out.println("Topic Changed?" + topicChange);
    System.out.println("Question Changed?" + questionChange);
      
    return ResponseEntity
    .status(HttpStatus.NO_CONTENT)
    .body(new ApiResponse<>(
      204,
      "Successfully updated topic",
      null 
    ));
  }

  public ResponseEntity getTopicWithQuestionsAndAnswers(UUID topicId){
    Topic topic = topicRepository.findById(topicId)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Topic not found" + topicId));
    
    List<Question> topicQuestions = questionRepository.findByTopicIdWithAnswers(topic.getId());
    topic.setQuestions(topicQuestions);

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    User user = (User) authentication.getPrincipal();

    Object formattedTopic;

    if(user.getRole().equals(Role.ADMIN)){
      //Returns an AdminTopicResponse with all fields
      formattedTopic = topicMapper.toAdminTopicResponse(topic);
    }
    else{
      //Returns a TopicResponse
      formattedTopic = topicMapper.toResponse(topic);
    }

    return ResponseEntity
    .status(HttpStatus.OK)
        .body(new ApiResponse<>(
        200,
        "Topic Retrieved",
        formattedTopic
    ));
  }
}
