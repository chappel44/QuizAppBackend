package com.tasks.organizer.organizationmember.entity;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.tasks.organizer.common.entity.BaseEntity;
import com.tasks.organizer.organization.entity.Organization;
import com.tasks.organizer.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder.Default;
import lombok.experimental.SuperBuilder;

@Table (name = "organization_members") @Entity
@Getter @Setter
@SuperBuilder (toBuilder = true)
@NoArgsConstructor
public class OrganizationMember extends BaseEntity {
  @ManyToOne (fetch = FetchType.LAZY)
  @OnDelete (action = OnDeleteAction.CASCADE)
  @JoinColumn (name = "organization_id", nullable = false)
  private Organization organization;

  @ManyToOne (fetch = FetchType.LAZY)
  @JoinColumn (
    name = "user_id",
    foreignKey = @ForeignKey (
      name = "fk_organization_members_user",
      foreignKeyDefinition = "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL"
    )
  )
  private User user;

  @Enumerated (EnumType.STRING)
  @Column (nullable = false)
  private OrgRoles role;

  @Default
  @Convert (converter = OrgPermissionsConverter.class)
  @Column (nullable = true)
  private OrgPermissions[] permissions = new OrgPermissions[0];

  @Default
  @Column (nullable = false)
  private boolean isActive = true;
}
