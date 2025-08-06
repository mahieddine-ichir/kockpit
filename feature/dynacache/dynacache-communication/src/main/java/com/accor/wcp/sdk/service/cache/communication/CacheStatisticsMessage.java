package com.accor.wcp.sdk.service.cache.communication;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class CacheStatisticsMessage implements Serializable {

  private final String __type__ = this.getClass().getName();
  private String name;
  private long timestamp;
  private long cacheGets;
  private long cacheHits;
  private long cacheMisses;
  private long cacheRemovals;
  private long cacheEvictions;
  private float averageGetTime;
  private float averagePutTime;
  private float averageRemoveTime;
  private float cacheHitPercentage;
  private float cacheMissPercentage;
}
