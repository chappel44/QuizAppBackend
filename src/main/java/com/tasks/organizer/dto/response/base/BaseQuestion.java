package com.tasks.organizer.dto.response.base;

import java.util.UUID;

import jakarta.persistence.MappedSuperclass;
import lombok.experimental.SuperBuilder;

@MappedSuperclass 
@SuperBuilder (toBuilder = true)
public class BaseQuestion {
  private UUID id;

  private int points;

  private String question;

  //private String answer;

  private String imageUrl;
}
