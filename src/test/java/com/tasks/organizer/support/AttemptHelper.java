package com.tasks.organizer.support;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;

import com.tasks.organizer.attempt.entity.Attempt;
import com.tasks.organizer.attempt.entity.AttemptQuestion;
import com.tasks.organizer.attempt.repository.AttemptQuestionRepository;
import com.tasks.organizer.attempt.repository.AttemptRepository;
import com.tasks.organizer.question.entity.Question;
import com.tasks.organizer.topic.entity.Topic;
import com.tasks.organizer.user.entity.User;

/**
 * Reusable attempt setup for integration tests.
 * Register it on a test class with {@code @Import(AttemptHelper.class)}.
 * Pair with {@link TopicHelper#createTopic} so the topic already has its questions attached.
 */
@TestComponent
public class AttemptHelper {
  @Autowired AttemptRepository attemptRepository;
  @Autowired AttemptQuestionRepository attemptQuestionRepository;

  // Creates an ungraded attempt for the user with one AttemptQuestion per question on the topic
  public Attempt createAttempt(Topic topic, User user) {
    double totalPoints = 0;
    for (Question question : topic.getQuestions()) {
      totalPoints += question.getPoints();
    }

    Attempt attempt = attemptRepository.save(Attempt.builder()
      .user(user)
      .topic(topic)
      .pointsEarned(0)
      .totalPoints(totalPoints)
      .percentage(0)
      .build());

    List<AttemptQuestion> attemptQuestions = new ArrayList<>();
    for (Question question : topic.getQuestions()) {
      attemptQuestions.add(attemptQuestionRepository.save(AttemptQuestion.builder()
        .attempt(attempt)
        .question(question)
        .build()));
    }

    attempt.setAttemptQuestions(attemptQuestions);

    return attempt;
  }

  // The single AttemptQuestion inserted by createAttempt when the topic came from TopicHelper
  public AttemptQuestion getAttemptQuestion(Attempt attempt) {
    return attempt.getAttemptQuestions().get(0);
  }
}
