package com.accor.wcp.console.services.cache;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CacheMetric {

  private String name;
  private Object value;
}
