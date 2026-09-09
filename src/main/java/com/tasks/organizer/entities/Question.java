package com.tasks.organizer.entities;

import com.tasks.organizer.entities.base.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Table (name = "questions")
@Entity 
@Getter 
@Setter 
@AllArgsConstructor 
@NoArgsConstructor 
public class Question extends BaseEntity {
  @NotNull 
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "topic_id", updatable = false, nullable = false)
  @Setter(AccessLevel.NONE)
  private Topic topic;

  @Min(0) 
  @Column (nullable = false)
  private int point;

  @NotNull
  @Size (min = 2, max = 255)
  @Column (length = 255, nullable = false)
  private String question;

  @NotNull
  @Size (min=2, max = 255)
  @Column (length = 255, nullable = false)
  private String answer;

  @Column (length = 2048, nullable = true)
  private String imageUrl;
}
