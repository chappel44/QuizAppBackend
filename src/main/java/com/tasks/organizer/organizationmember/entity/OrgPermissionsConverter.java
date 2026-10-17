package com.tasks.organizer.organizationmember.entity;

import java.util.Arrays;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class OrgPermissionsConverter implements AttributeConverter<OrgPermissions[], String[]> {
  @Override
  public String[] convertToDatabaseColumn(OrgPermissions[] permissions) {
    if (permissions == null) return new String[0];
    return Arrays.stream(permissions).map(Enum::name).toArray(String[]::new);
  }

  @Override
  public OrgPermissions[] convertToEntityAttribute(String[] permissions) {
    if (permissions == null) return new OrgPermissions[0];
    return Arrays.stream(permissions).map(OrgPermissions::valueOf).toArray(OrgPermissions[]::new);
  }
}
