package com.tasks.organizer.section.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import com.tasks.organizer.section.dto.response.SectionOverviewResponse;
import com.tasks.organizer.section.entity.Section;

@Mapper (componentModel = "spring")
public interface SectionMapperOverview {
  SectionOverviewResponse toResponse(Section section);

  List<SectionOverviewResponse> toResponse(List<Section> section);
}
