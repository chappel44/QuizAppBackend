package com.tasks.organizer.entities.base;

import jakarta.annotation.Nonnull;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@MappedSuperclass 
@AllArgsConstructor 
@NoArgsConstructor 
@Getter 
@Setter
public abstract class NamedEntity extends BaseEntity {
  @Nonnull 
  @Size (max = 50)
  private String name;

  @Column (nullable = true)
  private String description;
}
