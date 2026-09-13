package com.tasks.organizer.entities;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.tasks.organizer.entities.base.NamedEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.Builder.Default;
import lombok.experimental.SuperBuilder;

@Table (name = "topics")
@Entity 
@Getter 
@Setter 
@SuperBuilder
@NoArgsConstructor 
public class Topic extends NamedEntity {
  
  public enum TopicType {
    TEST, QUIZ, REVIEW
  }

  @NonNull
  @ManyToOne
  @JoinColumn (name = "section_id")
  private Section section;

  @OneToMany(mappedBy = "topic", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Question> questions;

  private TopicType topicType;

  @Default
  @Column (nullable = false)
  private Boolean isActive = true;

  @Column (name = "due_date")
  private LocalDateTime dueDate;
}
