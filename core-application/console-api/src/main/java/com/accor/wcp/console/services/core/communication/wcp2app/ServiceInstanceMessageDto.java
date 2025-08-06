package com.accor.wcp.console.services.core.communication.wcp2app;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
class ServiceInstanceMessageDto {
    @Builder.Default
    private String internalType = "INSTANCE";
    @Builder.Default
    private String messageId = UUID.randomUUID().toString();
    private String serviceId;
    private String domain;
    private String env;
    private String applicationId;
    private String instanceId;
    private Object message;
}
