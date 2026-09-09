package com.tasks.organizer.entities;

import java.time.LocalDateTime;

import com.tasks.organizer.entities.base.NamedEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;

@Entity 
public class Topic extends NamedEntity {
  public enum TopicType {
    TEST, QUIZ, REVIEW
  }

  @Column (name = "due_date")
  LocalDateTime dueDate;
}
