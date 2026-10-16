package com.tasks.organizer.question;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tasks.organizer.question.entity.Answer;

public interface AnswerRepository extends JpaRepository<Answer, UUID>{
}
