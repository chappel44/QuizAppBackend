package com.tasks.organizer.organization.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrganizationDTO {
  @NotNull
  @Size (max = 50)
  private String name;

  @Size (max = 255)
  private String description;

  @NotNull
  @Min (1)
  private Integer maxOrgAdmins;

  @NotNull
  @Min (0)
  private Integer maxStudents;

  @NotNull
  @Min (0)
  private Integer maxTeachers;
}
