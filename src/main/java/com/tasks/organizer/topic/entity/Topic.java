package com.tasks.organizer.topic.entity;

import com.tasks.organizer.attempt.entity.Attempt;
import com.tasks.organizer.question.entity.Question;
import com.tasks.organizer.section.entity.Section;
import com.tasks.organizer.user.entity.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.tasks.organizer.common.entity.NamedEntity;
import com.tasks.organizer.organization.entity.Organization;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.Builder.Default;
import lombok.experimental.SuperBuilder;

@Table (name = "topics") @Entity 
@Getter @Setter 
@SuperBuilder (toBuilder = true)
@AllArgsConstructor @NoArgsConstructor 
public class Topic extends NamedEntity {
  
  public enum TopicType {
    TEST, QUIZ, REVIEW, RANDOM_QUESTIONS
  }

  @NonNull
  @ManyToOne
  @JoinColumn (name = "section_id")
  private Section section;

  @Column(name = "section_id", insertable = false, updatable = false)
  private UUID sectionId;

  @OneToMany(mappedBy = "topic", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Question> questions;

  @OneToMany (mappedBy = "topic", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Attempt> attempts;
  
  @Default 
  @Min (1)
  private int version = 1;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private TopicType topicType;

  //Organization is optional, if null then it means it was published by a public user
  @ManyToOne(fetch = FetchType.LAZY, optional = true)
  @JoinColumn (name = "organization_id", nullable = true, updatable = false)
  private Organization organization;

  @Enumerated(EnumType.STRING)
  @Column (nullable = false, updatable = true)
  private Visibility visibility;

  //User who published the topic
  @ManyToOne (fetch = FetchType.LAZY, optional = false)
  @JoinColumn (name = "user_id", nullable = false, updatable = false)
  private User user;

  @Min (1)
  private Integer questionPoolSize;

  @Default
  @Column (nullable = false)
  private Boolean isActive = true;

  @Column (name = "due_date")
  private LocalDateTime dueDate;
}
