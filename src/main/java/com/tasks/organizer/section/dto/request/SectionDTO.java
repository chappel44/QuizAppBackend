package com.tasks.organizer.section.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter @NoArgsConstructor
public class SectionDTO {
  @NotNull 
  @Size (max = 50)
  private String name;

  @Size (max = 255)
  private String description;

  @NotNull
  private Boolean isActive;
}
