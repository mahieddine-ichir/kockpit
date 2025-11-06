package org.kockpit.features.featureflipping.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeatureFlippingDto {

    private String key;

    private Date expirationDate;

    private Boolean enabled;

    private String comment;
}
