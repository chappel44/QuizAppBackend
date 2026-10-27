package com.tasks.organizer.organizationmember;

import java.time.temporal.ChronoUnit;
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
import com.tasks.organizer.ratelimit.KeyType;
import com.tasks.organizer.ratelimit.RateLimit;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class OrganizationMemberController {
  final OrganizationMemberService organizationMemberService;

  /* ========== Authenticated Mappings ========== */
  @RateLimit(capacity = 15, period = 15, unit = ChronoUnit.MINUTES, keyType = KeyType.USER_ORGANIZATION_ID)
  @PostMapping ("/api/authenticated/organization/org-signup-token")
  public ResponseEntity<ApiResponse<Object>> createOrgSignupToken(@Validated @RequestBody OrgSignupTokenDTO request) {
    return organizationMemberService.createOrgSignupToken(request);
  }

  @RateLimit(capacity = 30, period = 15, unit = ChronoUnit.MINUTES, keyType = KeyType.USER)
  @PostMapping ("/api/authenticated/organization/org-signup-token/redeem")
  public ResponseEntity<ApiResponse<Object>> redeemOrgSignupToken(@RequestParam(required = true) UUID tokenId) {
    return organizationMemberService.redeemOrgSignupToken(tokenId);
  }
}
