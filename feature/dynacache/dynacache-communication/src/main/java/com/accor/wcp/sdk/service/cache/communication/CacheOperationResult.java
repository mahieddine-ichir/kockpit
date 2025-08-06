package com.accor.wcp.sdk.service.cache.communication;

import java.io.Serializable;

public enum CacheOperationResult implements Serializable {
  SENT,
  ACKED,
  DONE,
  ERROR;
}
