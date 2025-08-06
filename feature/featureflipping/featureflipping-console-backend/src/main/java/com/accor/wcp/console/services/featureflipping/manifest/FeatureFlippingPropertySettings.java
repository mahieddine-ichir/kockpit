package com.accor.wcp.console.services.featureflipping.manifest;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDate;

@Getter
@Setter
public class FeatureFlippingPropertySettings implements Serializable {
    private String key;
    private LocalDate expiration;
    private String label;
    private String description;
}
