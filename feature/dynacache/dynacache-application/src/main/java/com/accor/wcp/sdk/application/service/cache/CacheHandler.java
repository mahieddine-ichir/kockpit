package com.accor.wcp.sdk.application.service.cache;

import com.accor.wcp.sdk.service.cache.communication.CacheStatisticsMessage;
import java.util.List;

public interface CacheHandler {

  void reload(String cacheName);

  List<CacheStatisticsMessage> getCacheStatistics();

  void resetStats(String cache);
}
