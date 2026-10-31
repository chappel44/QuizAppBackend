package com.tasks.organizer.section.entity;

import com.tasks.organizer.topic.entity.Topic;
import com.tasks.organizer.user.entity.User;

import java.util.List;
import java.util.UUID;

import com.tasks.organizer.common.entity.NamedEntity;
import com.tasks.organizer.organization.entity.Organization;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder.Default;
import lombok.experimental.SuperBuilder;

@Table (name = "section")
@Entity
@SuperBuilder (toBuilder = true)
@NoArgsConstructor 
@Getter @Setter
public class Section extends NamedEntity {
  @Default
  @Column (nullable = false)
  private Boolean isActive = true;

  @OneToMany(mappedBy = "section", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Topic> topics;

  @ManyToOne (fetch = FetchType.LAZY)
  @JoinColumn (
    name = "organization_id",
    foreignKey = @ForeignKey (
      name = "fk_organization_sections",
      foreignKeyDefinition = "FOREIGN KEY (organization_id) REFERENCES organizations(id) ON DELETE CASCADE"
    ),
    nullable = true
  )
  private Organization organization;

  @ManyToOne (fetch = FetchType.LAZY)
  @JoinColumn (
    name = "user_id",
    foreignKey = @ForeignKey (
      name = "fk_users_sections",
      foreignKeyDefinition = "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE"
    ),
    nullable = true
  )
  private User user;
}
