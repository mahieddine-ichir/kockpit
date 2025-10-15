package org.kockpit.service.featureflipping.api;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FeatureFlippingHistory {
    private String key;
    private LocalDateTime timestamp;
    private String action;
    private String user;
}
