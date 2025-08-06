package com.accor.wcp.sdk.application.service.featureflipping;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
class DefaultFeatureFlippingKeyUpdateHandler implements FeatureFlippingKeyUpdateHandler {

  @Override
  public void update(String propertyName, String newValue) {
    log.info("Updating feature flipping key {} with new value {}", propertyName, newValue);
  }

}
