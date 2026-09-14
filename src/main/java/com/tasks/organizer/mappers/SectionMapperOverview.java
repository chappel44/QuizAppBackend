package com.tasks.organizer.mappers;

import java.util.List;

import org.mapstruct.Mapper;

import com.tasks.organizer.dto.response.SectionOverviewResponse;
import com.tasks.organizer.entities.Section;

@Mapper (componentModel = "spring")
public interface SectionMapperOverview {
  SectionOverviewResponse toResponse(Section section);

  List<SectionOverviewResponse> toResponse(List<Section> section);
}
