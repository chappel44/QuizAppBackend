package com.tasks.organizer.topic.entity;

import com.tasks.organizer.common.entity.BaseEntity;
import com.tasks.organizer.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Table (name = "topic_collaborators")
@Entity 
@SuperBuilder (toBuilder = true)
@Getter @Setter 
public class Collaborator extends BaseEntity {
  @JoinColumn (name = "user_id", nullable = false)
  @ManyToOne 
  User user;
  
  @JoinColumn (name = "topic_id", nullable = false)
  @ManyToOne
  Topic topic;
}
