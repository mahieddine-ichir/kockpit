package com.accor.wcp.console.services.featureflipping.manifest;

import java.util.List;
import lombok.Data;

@Data
public class FeatureFlippingSettingsDto {

  private String env;

  private String name;

  private String label;

  private List<FeatureFlippingPropertySettings> keys;
}
