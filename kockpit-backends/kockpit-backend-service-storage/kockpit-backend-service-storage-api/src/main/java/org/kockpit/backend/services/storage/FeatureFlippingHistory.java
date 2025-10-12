package org.kockpit.backend.services.storage;

import lombok.Data;

import java.time.LocalDateTime;


@Data
public class FeatureFlippingHistory {
    private String key;
    private String action;
    private String user;
    private LocalDateTime timestamp;
}
