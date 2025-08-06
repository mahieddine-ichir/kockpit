package com.accor.wcp.console.services.cache;

import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

@Data
public class CacheViewDto {

  List<InstanceCacheState> instanceCacheStates;
  List<String> columns;

  public CacheViewDto(CacheState cacheState) {
    final List<String> filteredColumns =
        List.of("__type__", "name", "cacheRemovals", "cacheEvictions", "averageGetTime", "averagePutTime", "averageRemoveTime");
    final List<String> addedColumns =
        List.of("cacheRemove", "cacheEvict", "avgGetT", "avgPutT", "avgRemoveT");

    this.instanceCacheStates = cacheState.getInstanceCacheStates().values().stream().toList();
    InstanceCacheState instanceCacheState = instanceCacheStates.stream().findFirst().orElse(null);
    if (nonNull(instanceCacheState)) {
      this.columns =
          instanceCacheState.getCacheMetrics().stream()
              .map(CacheMetric::getName)
              .filter(name -> !filteredColumns.contains(name))
              .collect(Collectors.toList());
      this.columns.addAll(addedColumns);
    }
  }
}
