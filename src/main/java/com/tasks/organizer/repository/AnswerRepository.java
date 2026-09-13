package com.tasks.organizer.repository;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.tasks.organizer.entities.Answer;

public interface AnswerRepository extends JpaRepository<Answer, UUID>{
}
