package com.tasks.organizer.organizationmember.service;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.tasks.organizer.common.dto.ApiResponse;
import com.tasks.organizer.organizationmember.dto.request.OrgSignupTokenDTO;

public interface OrganizationMemberService {
  ResponseEntity<ApiResponse<Object>> createOrgSignupToken(OrgSignupTokenDTO request, UUID organizationId);

  ResponseEntity<ApiResponse<Object>> redeemOrgSignupToken(UUID tokenId);
}
