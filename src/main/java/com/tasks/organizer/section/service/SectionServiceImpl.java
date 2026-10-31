package com.tasks.organizer.section.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.tasks.organizer.authorization.OrgAuthorization;
import com.tasks.organizer.common.dto.ApiResponse;
import com.tasks.organizer.organization.OrganizationRepository;
import com.tasks.organizer.organization.context.OrgContext;
import com.tasks.organizer.organization.entity.Organization;
import com.tasks.organizer.organizationmember.entity.OrgPermissions;
import com.tasks.organizer.section.SectionRepository;
import com.tasks.organizer.section.dto.request.SectionDTO;
import com.tasks.organizer.section.entity.Section;
import com.tasks.organizer.user.entity.User;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor  
public class SectionServiceImpl implements SectionService {
  final SectionRepository sectionRepository;
  final OrgAuthorization orgAuth;
  final OrganizationRepository orgRepository;

  public Section authorizeSectionOwnership(UUID sectionId, OrgPermissions permission){
    Section section = sectionRepository.findById(sectionId).orElse(null);

    if(section == null){
      return null;
    }

    OrgContext orgContext = orgAuth.currentOrgContext();
    User user = orgAuth.currentUser();

    Organization org = section.getOrganization();
    //If section belongs to an org they can modify it with permissions and verified permissions
    if (org != null && org.getId() != null) {
      if (orgContext == null
          || !orgAuth.hasPermissionVerified(org.getId(), permission)) {
        return null;
      }
    }
    // If section section isn't owned by an org
    // to update you must be the user who made the section
    else{
      if(!user.getId().equals(section.getUser().getId())){
        System.out.println("Section is not owned by authenticated user");
        return null;
      }
    }
    return section;
  }

  //This function is used to validate that a section being passed as param belongs 
  //to the authenticated organization member
  public Section validateOrgSectionOwnership(UUID sectionId){
    Section section = sectionRepository.findById(sectionId).orElse(null);
    if(section == null || !section.getIsActive()){
      return null;
    }
    Organization sectionOrg = section.getOrganization();
    if(sectionOrg != null && sectionOrg.getId() != null){
      OrgContext orgCtx = orgAuth.currentOrgContext();
      if(orgCtx == null || !orgCtx.organizationId().equals(sectionOrg.getId())){
        return null;
      }
    }
    else{
      if(!section.getUser().getId().equals(orgAuth.currentUser().getId())){
        return null;
      }
    }

    return section;
  }

  public ResponseEntity updateSection(UUID sectionId, SectionDTO request){
    Section section = authorizeSectionOwnership(sectionId, OrgPermissions.UPDATE_TOPIC);

    if(section == null){
      return ApiResponse.error(HttpStatus.NOT_FOUND, "Section not found");
    }

    //Cannot update the organization a section belongs to
    section.setName(request.getName());
    section.setDescription(request.getDescription());
    section.setIsActive(request.getIsActive());

    return ApiResponse.of(HttpStatus.NO_CONTENT, "Section updated successfully", null);
  }

  public Section addSection (SectionDTO request) {
    OrgContext orgContext = orgAuth.currentOrgContext();

    //If org is null by the time it saves section
    //then it belongs to a public user. 
    Organization org = null;

    if(orgContext != null && orgAuth.hasPermissionVerified(orgContext.organizationId(), OrgPermissions.CREATE_TOPIC)){
      org = orgRepository.findById(orgContext.organizationId()).orElse(null);

      if(org == null){
        System.out.println("Organization not found from the OrgContext token");
        return new Section();
      }
    }

    User user = orgAuth.currentUser(); //User defaults to the authenticated user in the request

    Section newSection = Section.builder()
    .name(request.getName())
    .organization(org)
    .user(user)
    .description(request.getDescription()).build();

    if(newSection == null){
      return new Section();
    }

    Section insertedSection = sectionRepository.save(newSection);

    return insertedSection;
  }
}
