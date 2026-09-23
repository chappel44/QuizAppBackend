package com.tasks.organizer.entities;

import java.util.UUID;

import com.tasks.organizer.entities.base.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder.Default;
import lombok.experimental.SuperBuilder;

@Entity
@Getter @Setter @SuperBuilder  @NoArgsConstructor 
public class AttemptQuestion extends BaseEntity {
  @ManyToOne (fetch = FetchType.LAZY)
  @JoinColumn(name = "attempt_id", updatable = false)
  private Attempt attempt;

  @ManyToOne (fetch = FetchType.LAZY)
  @JoinColumn(name = "question_id", updatable = false)
  private Question question;

  @Default 
  @Column (updatable = true)
  @Setter 
  private Boolean isCorrect = null;

  @Default 
  @Column (updatable = true)
  @Setter 
  private UUID submittedAnswerId = null;
}
