package com.accor.wcp.console.services.core.application.model;

import com.accor.wcp.console.sdk.topology.ManagedApplication;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class ApplicationDto implements ManagedApplication {
    private String applicationId;
    private String applicationName;
    private List<ApplicationInstanceDto> instances;
}
