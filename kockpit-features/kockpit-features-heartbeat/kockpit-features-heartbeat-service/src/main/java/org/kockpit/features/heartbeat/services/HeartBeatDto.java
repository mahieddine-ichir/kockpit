package org.kockpit.features.heartbeat.services;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HeartBeatDto {

    private String instanceId;

    private String appId;
}
