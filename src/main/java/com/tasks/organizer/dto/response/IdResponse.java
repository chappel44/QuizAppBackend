package com.tasks.organizer.dto.response;

import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Getter @Builder 
public class IdResponse {
  private UUID id;
}
