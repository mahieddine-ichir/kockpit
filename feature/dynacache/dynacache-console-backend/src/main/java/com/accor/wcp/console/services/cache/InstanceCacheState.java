package com.accor.wcp.console.services.cache;

import java.util.List;
import lombok.Data;

@Data
public class InstanceCacheState {
  private final String instanceId;
  private List<CacheMetric> cacheMetrics;
}
