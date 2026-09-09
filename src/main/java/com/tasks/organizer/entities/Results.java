package com.tasks.organizer.entities;

import org.hibernate.annotations.Check;

import com.tasks.organizer.entities.base.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor 
@NoArgsConstructor 
@Getter
@Setter 
public class Results extends BaseEntity {
  @Size (min = 0, max = 100)
  @Column
  @Check (constraints = "percentage >= 0 AND percentage <= 100")
  private double percentage;
  
  @Min(0)
  private double pointsEarned;

  @NotNull
  @ManyToOne (fetch = FetchType.LAZY)
  @JoinColumn (name = "user_id")
  private User user;

  @NotNull
  @ManyToOne (fetch = FetchType.LAZY)
  @JoinColumn (name = "topic_id")
  private Topic topic;
}
