package com.accor.wcp.console.services.dynaconfig.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DynaConfigDto {
    String id;
    Map<String, PropertyDto> properties = new HashMap<>();
    List<PropertyChangeDto> changes = new ArrayList<>();
    List<IssuedCommandDto> issuedCommands = new ArrayList<>();
}
