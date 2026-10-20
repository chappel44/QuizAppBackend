package com.tasks.organizer.organizationmember;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tasks.organizer.common.dto.ApiResponse;
import com.tasks.organizer.organizationmember.dto.request.OrgSignupTokenDTO;
import com.tasks.organizer.organizationmember.service.OrganizationMemberService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class OrganizationMemberController {
  final OrganizationMemberService organizationMemberService;

  /* ========== Authenticated Mappings ========== */
  @PostMapping ("/api/authenticated/organization/org-signup-token")
  public ResponseEntity<ApiResponse<Object>> createOrgSignupToken(@Validated @RequestBody OrgSignupTokenDTO request, @RequestParam(required = true) UUID organizationId) {
    return organizationMemberService.createOrgSignupToken(request, organizationId);
  }

  @PostMapping ("/api/authenticated/organization/org-signup-token/redeem")
  public ResponseEntity<ApiResponse<Object>> redeemOrgSignupToken(@RequestParam(required = true) UUID tokenId) {
    return organizationMemberService.redeemOrgSignupToken(tokenId);
  }
}
