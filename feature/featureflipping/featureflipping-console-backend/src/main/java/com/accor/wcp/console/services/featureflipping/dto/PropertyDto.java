package com.accor.wcp.console.services.featureflipping.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PropertyDto {
  Source source;
  Long lastUpdatedTimestamp;
  String key;
  String value;
  LocalDate expiration;
  String comment;
  String description;
  Status status;
  List<PropertyInstanceDto> instances = new ArrayList<>();
}
