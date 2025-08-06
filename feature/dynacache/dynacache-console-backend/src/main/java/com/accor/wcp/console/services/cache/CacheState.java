package com.accor.wcp.console.services.cache;

import java.util.HashMap;
import java.util.Map;
import lombok.Data;

@Data
class CacheState {

  private Map<String, InstanceCacheState> instanceCacheStates = new HashMap<>();
}
