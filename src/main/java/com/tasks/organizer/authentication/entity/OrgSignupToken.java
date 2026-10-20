package com.tasks.organizer.authentication.entity;

import java.time.Instant;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.tasks.organizer.common.entity.BaseEntity;
import com.tasks.organizer.organization.entity.Organization;
import com.tasks.organizer.organizationmember.entity.OrgPermissions;
import com.tasks.organizer.organizationmember.entity.OrgPermissionsConverter;
import com.tasks.organizer.organizationmember.entity.OrgRoles;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder.Default;
import lombok.experimental.SuperBuilder;

@Table (name = "org_signup_tokens") @Entity
@Getter @Setter
@SuperBuilder (toBuilder = true)
@NoArgsConstructor
public class OrgSignupToken extends BaseEntity {
  @ManyToOne (fetch = FetchType.LAZY)
  @OnDelete (action = OnDeleteAction.CASCADE)
  @JoinColumn (name = "organization_id", nullable = false)
  private Organization organization;

  @NotNull
  @Email
  @Size (max = 255)
  @Column (name = "invitee_email", nullable = false)
  private String inviteeEmail;

  @Default
  @Convert (converter = OrgPermissionsConverter.class)
  @Column (nullable = false)
  private OrgPermissions[] permissions = new OrgPermissions[0];

  @Enumerated (EnumType.STRING)
  @Column (nullable = false)
  private OrgRoles role;

  @NotNull
  @Column (name = "expires_at", nullable = false)
  private Instant expiresAt;
}
