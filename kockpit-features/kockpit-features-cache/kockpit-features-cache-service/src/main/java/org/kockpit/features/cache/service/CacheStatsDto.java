package org.kockpit.features.cache.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CacheStatsDto {

    private String domain;
    private String env;
    private String appId;
    private String name;

    private Long timestamp;
    private Long cacheGets;
    private Long cacheHits;
    private Long cacheMisses;
    private Long cacheRemovals;
    private Long cacheEvictions;
    private Double averageGetTime;
    private Double averagePutTime;
    private Double averageRemoveTime;
    private Double cacheHitPercentage;
    private Double cacheMissPercentage;
}