package com.accor.wcp.console.services.featureflipping.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class FeatureFlippingKeyDto {

    private String key;

    private LocalDate expiration;

    private String appId;
}
