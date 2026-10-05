package com.tasks.organizer.service.impl;

import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.tasks.organizer.controller.AdminController.ApiResponse;
import com.tasks.organizer.dto.request.SectionDTO;
import com.tasks.organizer.entities.Section;
import com.tasks.organizer.repository.SectionRepository;
import com.tasks.organizer.service.SectionService;

import lombok.AllArgsConstructor;

@AllArgsConstructor
@Service 
public class SectionServiceImpl implements SectionService {
  final SectionRepository sectionRepository;

  public ResponseEntity updateSection(UUID sectionId, SectionDTO request){
    if(sectionId == null){
        return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(new ApiResponse<>(
            400,
            "Missing section id",
            null 
        ));
    }
    
    Optional<Section> sectionOpt = sectionRepository.findById(sectionId);

    if(!sectionOpt.isPresent()){
        return ResponseEntity
        .status(HttpStatus.NOT_FOUND)
        .body(new ApiResponse<>(
            404,
            "Section not found",
            null 
        ));
    }

    Section section = sectionOpt.get();

    section.setName(request.getName());
    section.setDescription(request.getDescription());
    section.setIsActive(request.isActive());

    return ResponseEntity
    .status(HttpStatus.OK)
    .body(new ApiResponse<>(
        200,
        "Section updated successfully",
        null 
    ));
  }

  public Section addSection (SectionDTO request) {
    Section newSection = Section.builder()
    .name(request.getName())
    .description(request.getDescription()).build();

    if(newSection == null){
        return new Section();
    }

    Section insertedSection = sectionRepository.save(newSection);

    return insertedSection;
  }
}
