package com.tasks.organizer.dto.response.base;

import java.util.UUID;

import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@MappedSuperclass 
@SuperBuilder (toBuilder = true)
@Getter @Setter 
public class BaseQuestion {
  private UUID id;
  private String question;
  private int points;
  private String imageUrl;
}
