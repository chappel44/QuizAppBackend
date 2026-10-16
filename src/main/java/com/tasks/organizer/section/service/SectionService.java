package com.tasks.organizer.section.service;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.tasks.organizer.section.dto.request.SectionDTO;
import com.tasks.organizer.common.dto.ApiResponse;
import com.tasks.organizer.section.entity.Section;


public interface SectionService {
  ResponseEntity<ApiResponse<Object>> updateSection(UUID sectionId, SectionDTO request);
  Section addSection(SectionDTO request);
}
