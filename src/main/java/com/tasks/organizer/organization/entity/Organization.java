package com.tasks.organizer.organization.entity;

import com.tasks.organizer.common.entity.NamedEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder.Default;
import lombok.experimental.SuperBuilder;

@Table (name = "organizations") @Entity
@Getter @Setter
@SuperBuilder (toBuilder = true)
@NoArgsConstructor
public class Organization extends NamedEntity {
  @NotNull
  @Min (1)
  @Column (name = "max_org_admins", nullable = false)
  private Integer maxOrgAdmins;

  @NotNull
  @Min (0)
  @Column (name = "max_teachers", nullable = false)
  private Integer maxTeachers;

  @NotNull
  @Min (0)
  @Column (name = "max_students", nullable = false)
  private Integer maxStudents;

  @Default
  @Column (nullable = false)
  private Boolean active = true;
}