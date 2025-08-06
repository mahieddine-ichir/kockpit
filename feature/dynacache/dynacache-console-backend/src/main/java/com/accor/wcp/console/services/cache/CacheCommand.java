package com.accor.wcp.console.services.cache;

import com.accor.wcp.sdk.service.cache.communication.CacheInstanceOperationResult;
import java.io.Serializable;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Builder
@RequiredArgsConstructor
@Data
public class CacheCommand implements Serializable {

  private final long dateTime = Instant.now().toEpochMilli();
  private final Command command;
  private final Map<String, CacheInstanceOperationResult> instanceStatus = new HashMap<>();
}
