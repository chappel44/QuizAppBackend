package com.tasks.organizer.organizationmember.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public class OrgSignupTokenDTO {
  @NotBlank
  @Email
  @Size (max = 255)
  private String inviteeEmail;

  //Kept as a String so an invalid role can be rejected with a clear message, must match an OrgRoles value
  @NotBlank
  private String role;

  //Comma separated OrgPermissions, e.g. "CREATE_TOPIC,UPDATE_TOPIC". Only applies to TEACHER
  private String permissions;
}
