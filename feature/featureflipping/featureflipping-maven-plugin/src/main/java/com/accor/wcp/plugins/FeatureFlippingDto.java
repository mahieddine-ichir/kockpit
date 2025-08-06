package com.accor.wcp.plugins;

import lombok.Data;

import java.time.LocalDate;

@Data
public class FeatureFlippingDto {

    private String key;
    private LocalDate expiration;
    private String appId;
}
