package com.tasks.organizer.topic.service;

import java.util.ArrayList;
import java.util.Arrays;
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

import com.tasks.organizer.question.dto.request.AnswerDTO;
import com.tasks.organizer.topic.TopicRepository;
import com.tasks.organizer.topic.dto.request.CreateTopicDTO;
import com.tasks.organizer.question.dto.request.QuestionDTO;
import com.tasks.organizer.topic.dto.request.TopicDTO;
import com.tasks.organizer.question.entity.Answer;
import com.tasks.organizer.question.entity.Question;
import com.tasks.organizer.user.entity.Role;
import com.tasks.organizer.section.entity.Section;
import com.tasks.organizer.topic.entity.Topic;
import com.tasks.organizer.topic.entity.Visibility;
import com.tasks.organizer.organization.OrganizationRepository;
import com.tasks.organizer.organization.entity.Organization;
import com.tasks.organizer.organizationmember.OrganizationMemberRepository;
import com.tasks.organizer.organizationmember.entity.OrgPermissions;
import com.tasks.organizer.organizationmember.entity.OrgRoles;
import com.tasks.organizer.organizationmember.entity.OrganizationMember;
import com.tasks.organizer.user.entity.User;
import com.tasks.organizer.topic.entity.Topic.TopicType;
import com.tasks.organizer.topic.mapper.TopicMapper;
import com.tasks.organizer.question.AnswerRepository;
import com.tasks.organizer.question.QuestionRepository;
import com.tasks.organizer.section.SectionRepository;
import com.tasks.organizer.common.dto.ApiResponse;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

@Service 
@AllArgsConstructor
public class TopicServiceImpl implements TopicService {
  final TopicRepository topicRepository;
  final SectionRepository sectionRepository;
  final QuestionRepository questionRepository;
  final AnswerRepository answerRepository;
  final OrganizationRepository organizationRepository;
  final TopicMapper topicMapper;
  final OrganizationMemberRepository organizationMemberRepository;
  
  private static final int ANSWER_LIMIT = 5;

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
      if(count > ANSWER_LIMIT){
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Max answer count of " + ANSWER_LIMIT + " exceeded.");
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

  //ORG_ADMIN is always allowed, TEACHER must hold the required permission, everyone else is rejected
  //Non members and members without permission get a 404 so the resource isn't revealed
  private void checkOrgPermission(Organization organization, User user, OrgPermissions requiredPermission, String notFoundMessage) {
    OrganizationMember member = organizationMemberRepository
      .findByOrganizationIdAndUserId(organization.getId(), user.getId())
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, notFoundMessage));

    if(!member.isActive()){
      System.out.println("Member is inactive");
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Organization membership is inactive");
    }

    boolean allowed = switch (member.getRole()) {
      case ORG_ADMIN -> true;
      case TEACHER -> Arrays.asList(member.getPermissions()).contains(requiredPermission);
      default -> false;
    };
    if(!allowed){
      System.out.println("MISSING THE PERMISSION " + requiredPermission);
      throw new ResponseStatusException(HttpStatus.NOT_FOUND, notFoundMessage);
    }
  }

  @Transactional 
  ResponseEntity<ApiResponse<Object>> removeTopic(UUID topicId){
    topicRepository.deleteById(topicId);
    return ApiResponse.of(HttpStatus.NO_CONTENT, "Topic deleted", null);
  }

  @Transactional 
  public String createTopic(CreateTopicDTO entity, UUID sectionId, UUID organizationId){
      Section section = sectionRepository.findById(sectionId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Section not found" + sectionId));

      if(!section.getIsActive()){
        throw new ResponseStatusException(HttpStatus.CONFLICT, "This section is marked inactive " + sectionId);
      }

      TopicDTO topicDTO = entity.getTopic();

      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      User user = (User) authentication.getPrincipal();

      //Public user topics have no organization and are always PUBLIC
      Organization organization = null;
      Visibility visibility = Visibility.PUBLIC;

      if(organizationId != null){
        organization = organizationRepository.findById(organizationId)
          .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Organization not found " + organizationId));

        checkOrgPermission(organization, user, OrgPermissions.CREATE_TOPIC, "Section not found" + sectionId);

        if(entity.getTopic().getVisibility() == null){
          throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Visibility is required for organization topics");
        }
        visibility = entity.getTopic().getVisibility();
      }

      //Construct a new Topic
      Topic newTopic = Topic.builder()
      .section(section)
      .organization(organization)
      .visibility(visibility)
      .user(user)
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
  public ResponseEntity<ApiResponse<Object>> updateTopic(UUID topicId, CreateTopicDTO request) {
    Topic activeTopic = topicRepository.findById(topicId)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Topic not found" + topicId));

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    User user = (User) authentication.getPrincipal();

    Organization organization = activeTopic.getOrganization();
    if(organization != null){
      checkOrgPermission(organization, user, OrgPermissions.UPDATE_TOPIC, "Topic not found " + topicId);

      //Visibility only applies to organization topics and does not affect the version
      if(request.getTopic().getVisibility() != null){
        activeTopic.setVisibility(request.getTopic().getVisibility());
      }
    }
    else{
      //Only return 404 for topics owned by a public user
      if(!activeTopic.getUser().getId().equals(user.getId())){
        return ApiResponse.of(HttpStatus.NOT_FOUND, "topic not found", null);
      }
    }

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
              return ApiResponse.of(HttpStatus.BAD_REQUEST, "Duplicate correct answer found on question ", requestAnswer);
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
          if(count > ANSWER_LIMIT){
            return ApiResponse.of(HttpStatus.BAD_REQUEST, "Max answer count of " + ANSWER_LIMIT + " exceeded.", activeQuestion.getId());
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

    return ApiResponse.of(HttpStatus.NO_CONTENT, "Successfully updated topic", null);
  }

  @org.springframework.transaction.annotation.Transactional (readOnly = true) //prevents flushing the questions
  public ResponseEntity<ApiResponse<Object>> getTopicWithQuestionsAndAnswers(UUID topicId){
    Topic topic = topicRepository.findById(topicId).orElse(null);
    if(topic == null){
      return ApiResponse.error(HttpStatus.NOT_FOUND, "Topic not found " + topicId);
    }

    List<Question> topicQuestions = questionRepository.findByTopicIdWithAnswers(topic.getId());
    topic.setQuestions(topicQuestions);

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    User user = (User) authentication.getPrincipal();

    //Site admins always see the correct answers
    if(user.getRole().equals(Role.SITE_ADMIN)){
      return ApiResponse.ok("Topic Retrieved", topicMapper.toAdminTopicResponse(topic));
    }

    Object formattedTopic;
    Organization organization = topic.getOrganization();
    OrganizationMember member = organization == null ? null : organization.getOrganizationMembers().stream()
      .filter(m -> m.isActive() && m.getUser() != null && Objects.equals(m.getUser().getId(), user.getId()))
      .findFirst()
      .orElse(null);

    if(organization != null && topic.getVisibility() == Visibility.PRIVATE){
      //Non members get the same 404 as a missing topic so private topics aren't revealed
      if(member == null){
        return ApiResponse.error(HttpStatus.NOT_FOUND, "Topic not found" + topicId);
      }
      formattedTopic = switch (member.getRole()) {
        case ORG_ADMIN, TEACHER -> topicMapper.toAdminTopicResponse(topic);
        case STUDENT -> topicMapper.toResponse(topic);
      };
    }
    else if(member != null && (member.getRole() == OrgRoles.ORG_ADMIN || member.getRole() == OrgRoles.TEACHER)) {
      //Org admins and teachers see the correct answers on their organization's public topics
      formattedTopic = topicMapper.toAdminTopicResponse(topic);
    }
    else{
      //Owners see the correct answers, everyone else gets the student response
      boolean isOwner = topic.getUser() != null && Objects.equals(topic.getUser().getId(), user.getId());
      formattedTopic = isOwner ? topicMapper.toAdminTopicResponse(topic) : topicMapper.toResponse(topic);
    }

    return ApiResponse.ok("Topic Retrieved", formattedTopic);
  }

  public ResponseEntity<ApiResponse<Object>> deleteTopic(UUID topicId) {
    Topic topic = topicRepository.findById(topicId).orElse(null);
    if(topic == null){
      System.out.println("NO TOPIC FOUND");
      return ApiResponse.error(HttpStatus.NOT_FOUND, "Topic not found " + topicId);
    }

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    User user = (User) authentication.getPrincipal();

    //Site admins always see the correct answers
    if(user.getRole().equals(Role.SITE_ADMIN)){
      return removeTopic(topicId);
    }
    
    Organization organization = topic.getOrganization();
    if(organization != null) {
      OrganizationMember member = organizationMemberRepository
        .findByOrganizationIdAndUserId(organization.getId(), user.getId())
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Topic not found"));

      if(member.getRole() == OrgRoles.ORG_ADMIN){
        return removeTopic(topicId);
      }
      else if(member.getRole() == OrgRoles.TEACHER){
        checkOrgPermission(organization, user, OrgPermissions.DELETE_TOPIC, "Topic not found");
        return removeTopic(topicId);
      }
      //Student attempted to delete topic
      System.out.println("STUDENT ATTEMPTED TO DELETE A TOPIC");
      return ApiResponse.error(HttpStatus.NOT_FOUND, "Topic not found " + topicId);
    }

    if(topic.getUser().equals(user)){
      return removeTopic(topicId);
    }

    System.err.println("END OF DELETE HIT");

    return ApiResponse.error(HttpStatus.NOT_FOUND, "Topic not found " + topicId);
  }
}
