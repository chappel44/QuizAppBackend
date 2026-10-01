package com.tasks.organizer.entities;

import java.util.List;

import com.tasks.organizer.entities.base.BaseEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.Builder.Default;
import lombok.experimental.SuperBuilder;

@Entity 
@Table (name = "attempt")
@SuperBuilder 
@NoArgsConstructor 
@Getter @Setter @ToString 
public class Attempt extends BaseEntity {
  @ManyToOne (fetch = FetchType.LAZY)
  @JoinColumn(name = "topic_id", updatable = false, nullable = false)
  @Setter (AccessLevel.NONE)
  private Topic topic;

  @ManyToOne (fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", updatable = false, nullable = false)
  @Setter (AccessLevel.NONE)
  private User user;

  @OneToMany (mappedBy = "attempt", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<AttemptQuestion> attemptQuestions;

  @Min (0)
  private double pointsEarned;

  @Min (0)
  private double totalPoints;

  @DecimalMin(value = "0.0", inclusive = true, message = "Percentage must be at least 0")
  @DecimalMax(value = "1.0", inclusive = true, message = "Percentage must be at most 1")
  private double percentage;

  @Default
  @Column (nullable = false)
  private boolean isFinalized = false;
}
