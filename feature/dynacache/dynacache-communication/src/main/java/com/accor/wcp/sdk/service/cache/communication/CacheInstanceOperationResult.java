package com.accor.wcp.sdk.service.cache.communication;

import java.io.Serializable;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class CacheInstanceOperationResult implements Serializable {

  private long dateTime;
  private CacheOperationResult result;
}
