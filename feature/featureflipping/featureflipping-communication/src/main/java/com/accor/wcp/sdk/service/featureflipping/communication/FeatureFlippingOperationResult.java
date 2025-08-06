package com.accor.wcp.sdk.service.featureflipping.communication;

import java.io.Serializable;

public enum FeatureFlippingOperationResult implements Serializable {
  SENT,
  ACKED,
  DONE,
  ERROR;
}
