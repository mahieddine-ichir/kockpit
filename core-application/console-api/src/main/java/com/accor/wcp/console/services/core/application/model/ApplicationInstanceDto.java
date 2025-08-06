package com.accor.wcp.console.services.core.application.model;

import com.accor.wcp.console.sdk.topology.MangedApplicationInstance;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class ApplicationInstanceDto implements MangedApplicationInstance {
    private String instanceId;
    private String applicationVersion;
    private long lastUpdatedTimestamp;
}
