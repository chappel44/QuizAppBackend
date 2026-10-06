package com.tasks.organizer.entities;

import com.tasks.organizer.entities.base.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.Builder.Default;
import lombok.experimental.SuperBuilder;

@Entity 
@Table (name = "answers")
@Getter @Setter 
@SuperBuilder 
@NoArgsConstructor 
@ToString 
public class Answer extends BaseEntity{
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "question_id", updatable = false, nullable = false)
  @Setter(AccessLevel.NONE)
  private Question question;

  @NotNull
  @Column (nullable = false)
  @Size (min=2, max = 255)
  private String answer;

  @Column (nullable = false)
  private Boolean isCorrect;

  @Default 
  @Column (nullable = false)
  private boolean isActive = true;
}
