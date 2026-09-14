package com.tasks.organizer.entities.base;

import jakarta.annotation.Nonnull;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@MappedSuperclass 
@Getter @Setter
@SuperBuilder (toBuilder = true)
@NoArgsConstructor 
public abstract class NamedEntity extends BaseEntity {
  @Nonnull 
  @Size (max = 50)
  private String name;

  @Column (nullable = true)
  @Size (max = 255)
  private String description;
}
