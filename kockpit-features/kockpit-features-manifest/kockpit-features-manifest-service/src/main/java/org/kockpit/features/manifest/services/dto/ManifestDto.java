package org.kockpit.features.manifest.services.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ManifestDto {

    private String domain;

    private String env;

    private String name;

    private List<ServiceDto> services;

    private List<PolicyDto> policies;
}
