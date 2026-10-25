package com.tasks.organizer.support;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;

import com.tasks.organizer.organization.entity.Organization;
import com.tasks.organizer.question.AnswerRepository;
import com.tasks.organizer.question.QuestionRepository;
import com.tasks.organizer.question.entity.Answer;
import com.tasks.organizer.question.entity.Question;
import com.tasks.organizer.section.SectionRepository;
import com.tasks.organizer.section.entity.Section;
import com.tasks.organizer.topic.TopicRepository;
import com.tasks.organizer.topic.entity.Topic;
import com.tasks.organizer.topic.entity.Topic.TopicType;
import com.tasks.organizer.topic.entity.Visibility;
import com.tasks.organizer.user.entity.User;

/**
 * Reusable topic setup for integration tests.
 * Register it on a test class with {@code @Import(TopicHelper.class)}.
 */
@TestComponent
public class TopicHelper {
  @Autowired SectionRepository sectionRepository;
  @Autowired TopicRepository topicRepository;
  @Autowired QuestionRepository questionRepository;
  @Autowired AnswerRepository answerRepository;

  public Section createSection(String name) {
    return sectionRepository.save(Section.builder()
      .name(name + " " + UUID.randomUUID())
      .description(name)
      .build());
  }
  
  public Topic createTopic(User user, Visibility visibility, TopicType topicType, Organization organization) {
    Section section = createSection("Test section");

    Topic topic = topicRepository.save(Topic.builder()
      .name("Test topic " + UUID.randomUUID())
      .description("Test description")
      .section(section)
      .topicType(topicType)
      .organization(organization)
      .user(user)
      .visibility(visibility)
      .questionPoolSize(1)
      .build());

    Question question = questionRepository.save(Question.builder()
      .question("Test question")
      .points(1)
      .topic(topic)
      .build());

    Answer answer = answerRepository.save(Answer.builder()
      .answer("Test answer")
      .isCorrect(true)
      .question(question)
      .build());

    question.setAnswers(List.of(answer));
    topic.setQuestions(List.of(question));

    return topic;
  }

  public Topic createTopic(User user, Visibility visibility, TopicType topicType) {
    return createTopic(user, visibility, topicType, null);
  }

  // The single question inserted by createTopic
  public Question getQuestion(Topic topic) {
    return topic.getQuestions().get(0);
  }

  // The single answer inserted by createTopic
  public Answer getAnswer(Topic topic) {
    return getQuestion(topic).getAnswers().get(0);
  }
}
