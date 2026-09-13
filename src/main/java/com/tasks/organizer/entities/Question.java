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
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Table (name = "questions")
@Entity 
@Getter @Setter 
@SuperBuilder
@NoArgsConstructor 
public class Question extends BaseEntity {
  @NotNull 
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "topic_id", updatable = false, nullable = false)
  @Setter(AccessLevel.NONE)
  private Topic topic;

  @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Answer> answers;

  @Min(0) 
  @Column (nullable = false)
  private double points;

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
