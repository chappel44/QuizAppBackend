package com.tasks.organizer.section;

import com.tasks.organizer.authorization.OrgAuthorization;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tasks.organizer.common.dto.ApiResponse;
import com.tasks.organizer.organization.context.OrgContext;
import com.tasks.organizer.organization.entity.Organization;
import com.tasks.organizer.organizationmember.entity.OrgPermissions;
import com.tasks.organizer.ratelimit.KeyType;
import com.tasks.organizer.ratelimit.RateLimit;
import com.tasks.organizer.section.dto.request.SectionDTO;
import com.tasks.organizer.section.dto.response.SectionOverviewResponse;
import com.tasks.organizer.section.entity.Section;
import com.tasks.organizer.section.mapper.SectionMapperOverview;
import com.tasks.organizer.section.service.SectionService;
import com.tasks.organizer.topic.entity.Topic;
import com.tasks.organizer.user.entity.Role;
import com.tasks.organizer.user.entity.User;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequiredArgsConstructor
public class SectionController {
    private final OrgAuthorization orgAuthorization;
    final SectionRepository sectionRepository;
    final SectionService sectionService;
    final SectionMapperOverview sectionMapperOverview;

    /* ========== Admin Mappings ========== */
    @RateLimit(capacity = 100, period = 1, unit = ChronoUnit.HOURS, keyType = KeyType.USER_ORGANIZATION_ID)
    @PostMapping("/api/authenticated/section")
    public Section addSection(@Validated @RequestBody SectionDTO request) {
        return sectionService.addSection(request);
    }

    @RateLimit(capacity = 50, period = 30, unit = ChronoUnit.MINUTES, keyType = KeyType.USER_ORGANIZATION_ID)
    @DeleteMapping ("/api/authenticated/section")
    public ResponseEntity<ApiResponse<Object>> deleteSection(@RequestParam(required = true) UUID sectionId){
        Section section = sectionService.authorizeSectionOwnership(sectionId, OrgPermissions.DELETE_TOPIC);

        if(section == null){
            return ApiResponse.error(HttpStatus.NOT_FOUND, "Section not found");
        }

        sectionRepository.deleteById(sectionId);
        return ApiResponse.of(HttpStatus.NO_CONTENT, "Section deleted", null);
    }

    @Transactional
    @RateLimit(capacity = 100, period = 1, unit = ChronoUnit.HOURS, keyType = KeyType.USER_ORGANIZATION_ID)
    @PatchMapping ("/api/authenticated/section")
    public ResponseEntity updateSection( @RequestParam(required = true) UUID sectionId, @Valid @RequestBody @NotNull SectionDTO request) {
        return sectionService.updateSection(sectionId, request);
    }

    /* ========== Authenticated Mappings ========== */
    @RateLimit(capacity = 100, period = 1, unit = ChronoUnit.HOURS, keyType = KeyType.USER_ORGANIZATION_ID)
    @GetMapping("/api/authenticated/section-overview")
    public ResponseEntity<ApiResponse<Object>> getSectionsWithTopics() {
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      User user = (User) authentication.getPrincipal();

      List<Section> sections = sectionRepository.findAllWithTopics();

      if(user.getRole().equals(Role.USER)){
        sections = sections.stream()
          .filter(Section::getIsActive)
          .map(section -> section.toBuilder().build())
          .peek(copy -> {
              List<Topic> activeTopics = copy.getTopics().stream()
                .filter(Topic::getIsActive)
                .collect(Collectors.toList());
              copy.setTopics(activeTopics);
          })
          .filter(copy -> !copy.getTopics().isEmpty())
          .collect(Collectors.toList());
      }

      List<SectionOverviewResponse> overview = sectionMapperOverview.toResponse(sections);

      return ApiResponse.ok("Section overview", overview);
    }
}
