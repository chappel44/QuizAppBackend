package com.tasks.organizer.section.entity;

import com.tasks.organizer.topic.entity.Topic;

import java.util.List;

import com.tasks.organizer.common.entity.NamedEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder.Default;
import lombok.experimental.SuperBuilder;

@Table (name = "section")
@Entity
@SuperBuilder (toBuilder = true)
@NoArgsConstructor 
@Getter @Setter
public class Section extends NamedEntity {
  @Default
  @Column (nullable = false)
  private Boolean isActive = true;

  @OneToMany(mappedBy = "section", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Topic> topics;
}
