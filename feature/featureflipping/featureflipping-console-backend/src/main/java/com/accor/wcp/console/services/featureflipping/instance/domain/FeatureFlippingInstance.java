package com.accor.wcp.console.services.featureflipping.instance.domain;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(exclude = "currentValue")
public class FeatureFlippingInstance {
  private String applicationInstance;
  private String propertyKey;
  private String currentValue;
}
