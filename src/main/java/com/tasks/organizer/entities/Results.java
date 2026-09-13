package com.tasks.organizer.entities;

import org.hibernate.annotations.Check;

import com.tasks.organizer.entities.base.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Table (name = "results")
@Entity 
@Getter
@Setter
@SuperBuilder 
public class Results extends BaseEntity {
  @Column
  @Check (constraints = "percentage >= 0 AND percentage <= 1")
  private double percentage;
  
  @Min(0)
  private double pointsEarned;

  @Min(0)
  private double totalPoints;

  @NotNull
  @ManyToOne (fetch = FetchType.LAZY)
  @JoinColumn (name = "user_id")
  private User user;

  @NotNull
  @ManyToOne (fetch = FetchType.LAZY)
  @JoinColumn (name = "topic_id")
  private Topic topic;
}
