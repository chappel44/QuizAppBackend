package com.tasks.organizer.organizationmember.entity;

import java.util.Arrays;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

//Stores permissions as a comma separated string, e.g. "CREATE_TOPIC,UPDATE_TOPIC"
@Converter
public class OrgPermissionsConverter implements AttributeConverter<OrgPermissions[], String> {
  private static final String DELIMITER = ",";

  @Override
  public String convertToDatabaseColumn(OrgPermissions[] permissions) {
    if (permissions == null) return "";
    return String.join(DELIMITER, Arrays.stream(permissions).map(Enum::name).toArray(String[]::new));
  }

  @Override
  public OrgPermissions[] convertToEntityAttribute(String permissions) {
    if (permissions == null || permissions.isBlank()) return new OrgPermissions[0];
    return Arrays.stream(permissions.split(DELIMITER))
      .map(String::trim)
      .filter(p -> !p.isEmpty())
      .map(OrgPermissions::valueOf)
      .toArray(OrgPermissions[]::new);
  }
}
