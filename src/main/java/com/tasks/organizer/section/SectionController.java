package com.tasks.organizer.section;

import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tasks.organizer.common.dto.ApiResponse;
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
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
    final SectionRepository sectionRepository;
    final SectionService sectionService;
    final SectionMapperOverview sectionMapperOverview;

    /* ========== Admin Mappings ========== */
    @RateLimit(capacity = 100, period = 1, unit = ChronoUnit.HOURS, keyType = KeyType.USER_ORGANIZATION_ID)
    @PostMapping("/api/admin/section")
    public Section addSection(@Validated @RequestBody SectionDTO request) {
        return sectionService.addSection(request);
    }

    @RateLimit(capacity = 50, period = 30, unit = ChronoUnit.MINUTES, keyType = KeyType.USER_ORGANIZATION_ID)
    @DeleteMapping ("/api/admin/section")
    public String deleteSection(@RequestParam  @NotNull UUID sectionId){
        if(sectionId == null){
            return "Section id is required";
        }

        sectionRepository.deleteById(sectionId);
        return "Section deleted successfully";
    }

    @Transactional
    @RateLimit(capacity = 100, period = 1, unit = ChronoUnit.HOURS, keyType = KeyType.USER_ORGANIZATION_ID)
    @PatchMapping ("/api/admin/section")
    public ResponseEntity updateSection( @RequestParam @NotNull UUID sectionId, @RequestBody @NotNull SectionDTO request) {
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
