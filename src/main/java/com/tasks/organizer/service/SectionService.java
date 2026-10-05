package com.tasks.organizer.service;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

import com.tasks.organizer.dto.request.SectionDTO;
import com.tasks.organizer.entities.Section;


public interface SectionService {
  ResponseEntity updateSection(UUID sectionId, SectionDTO request);
  Section addSection(SectionDTO request);
}
