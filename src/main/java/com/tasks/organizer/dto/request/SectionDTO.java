package com.tasks.organizer.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;


@Getter
public class SectionDTO {
  @NotNull 
  @Size (max = 50)
  private String name;

  @Size (max = 255)
  private String description;

  private boolean isActive = true;
}
