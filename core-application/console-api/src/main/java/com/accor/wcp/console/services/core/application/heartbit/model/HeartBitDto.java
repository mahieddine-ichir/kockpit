package com.accor.wcp.console.services.core.application.heartbit.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HeartBitDto {
    private String domain;
    private String env;
    private String applicationId;
    private String applicationInstance;
    private String applicationVersion;
    private long timestamp = System.currentTimeMillis();
    private boolean ended;
}
