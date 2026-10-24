package com.tasks.organizer.authentication.service;

import com.tasks.organizer.authentication.dto.request.SignUpRequest;
import com.tasks.organizer.authentication.dto.request.SigninRequest;
import com.tasks.organizer.authentication.dto.response.JwtAuthenticationResponse;
import com.tasks.organizer.common.dto.ApiResponse;
import com.tasks.organizer.organization.OrganizationRepository;
import com.tasks.organizer.organization.dto.OrganizationDTO;
import com.tasks.organizer.organization.entity.Organization;
import com.tasks.organizer.organizationmember.OrganizationMemberRepository;
import com.tasks.organizer.organizationmember.entity.OrgPermissions;
import com.tasks.organizer.organizationmember.entity.OrgRoles;
import com.tasks.organizer.organizationmember.entity.OrganizationMember;
import com.tasks.organizer.user.entity.Role;
import com.tasks.organizer.user.entity.User;
import com.tasks.organizer.user.UserRepository;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
    private final OrganizationMemberRepository organizationMemberRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final OrganizationRepository organizationRepository;

    private Map<String, Object> generateClaims(User user){
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("id", user.getId());
        extraClaims.put("role", user.getRole().toString());
        return extraClaims;
    }

    private Map<String, Object> generateOrgClaims(UUID userId, OrgPermissions[] orgPermissions, OrgRoles role, UUID organizationId){
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("id", userId);
        extraClaims.put("organizationId", organizationId);
        extraClaims.put("orgPermissions", orgPermissions);
        extraClaims.put("role", role);
        return extraClaims;
    }

    private void createOrganization(OrganizationDTO organizationDTO, User owner){
        Organization organization = Organization.builder()
            .name(organizationDTO.getName())
            .description(organizationDTO.getDescription())
            .maxOrgAdmins(organizationDTO.getMaxOrgAdmins())
            .maxStudents(organizationDTO.getMaxStudents())
            .maxTeachers(organizationDTO.getMaxTeachers())
            .build();

        //ORG_ADMIN is always allowed in checkOrgPermission so no explicit permissions are needed
        OrganizationMember member = OrganizationMember.builder()
            .organization(organization)
            .user(owner)
            .role(OrgRoles.ORG_ADMIN)
            .build();
        organization.getOrganizationMembers().add(member);

        organizationRepository.save(organization);
    }

    @Override
    @Transactional
    public ResponseEntity<JwtAuthenticationResponse> signup(SignUpRequest request) {
        Optional<User> existingUser = userRepository.findByEmail(request.getEmail());
        if(existingUser.isPresent()){
            return ResponseEntity.badRequest().build();
        }

        if(request.getPassword().length() < 2){
            return ResponseEntity.badRequest().build();
        }

        var newUser = User.builder().firstName(request.getFirstName()).lastName(request.getLastName())
            .email(request.getEmail()).password(passwordEncoder.encode(request.getPassword()))
            .role(Role.USER).build();

        User user = userRepository.save(newUser);

        if(request.getOrganization() != null){
            createOrganization(request.getOrganization(), user);
        }

        var jwt = jwtService.generateToken(generateClaims(user), user);
        return ResponseEntity.ok(
            JwtAuthenticationResponse.builder()
                .token(jwt)
                .build()
        );
    }

    @Override
    public JwtAuthenticationResponse signin(SigninRequest request) {
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        var user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new IllegalArgumentException("Invalid email or password."));
        
        if(!user.isActive()){
            throw new IllegalArgumentException("Invalid email or password.");
        }

        var jwt = jwtService.generateToken(generateClaims(user), user);
        return JwtAuthenticationResponse.builder().token(jwt).build();
    }

    @Override 
    public ResponseEntity<ApiResponse<Object>> createOrgToken(UUID organizationId){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();

        OrganizationMember member = organizationMemberRepository.findByOrganizationIdAndUserId(organizationId, user.getId()).orElse(null);

        if(member == null){
            return ApiResponse.error(HttpStatus.NOT_FOUND, "Active member not found");
        }

        // if(member.isActive() == false){
        //     return ApiResponse.error(HttpStatus.NOT_FOUND, "Active member not found");
        // }

        OrgPermissions[] permissions = member.getPermissions();
        OrgRoles orgRole = member.getRole();
        UUID userOrg = organizationId;

        var jwt = jwtService.generateToken(generateOrgClaims(user.getId(), permissions, orgRole, userOrg), user);

        return ApiResponse.ok("Created token", JwtAuthenticationResponse.builder().token(jwt).build());
    }
}
