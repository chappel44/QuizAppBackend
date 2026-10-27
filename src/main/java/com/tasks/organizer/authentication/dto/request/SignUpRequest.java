package com.tasks.organizer.authentication.dto.request;

import com.tasks.organizer.organization.dto.OrganizationDTO;
import com.tasks.organizer.user.entity.Role;

import jakarta.validation.Valid;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignUpRequest {
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private Role role;

    //Optional, when present an organization is created with the new user as its ORG_ADMIN
    @Valid
    private OrganizationDTO organization;
}