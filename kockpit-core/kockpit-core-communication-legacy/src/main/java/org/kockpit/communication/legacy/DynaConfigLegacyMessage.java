package org.kockpit.communication.legacy;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DynaConfigLegacyMessage {

    private String internalType;
    private String messageId;
    private String serviceId;
    private String domain;
    private String env;
    private String applicationId;
    private String instanceId;
    private InstanceInitPropertiesUpdateRequestDto message;
}
