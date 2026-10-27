package com.tasks.organizer.authentication.entity;

import java.time.LocalDateTime;

import com.tasks.organizer.common.entity.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Entity 
@Table (name = "password_reset_tokens")
@Getter @Setter 
@SuperBuilder 
@NoArgsConstructor
public class PasswordResetToken extends BaseEntity{
  @Column(name = "expires_at", nullable = false)
  private LocalDateTime expiresAt;

  @Column (name = "token_hash", nullable = false, unique = true)
  private String tokenHash;

  @Column (name = "email", nullable = false, unique = true)
  private String email;
}
