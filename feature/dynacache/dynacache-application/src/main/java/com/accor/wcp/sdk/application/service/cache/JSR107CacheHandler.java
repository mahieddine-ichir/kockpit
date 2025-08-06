package com.accor.wcp.sdk.application.service.cache;

import com.accor.wcp.sdk.application.communication.App2WCPConsoleCommunicationService;
import com.accor.wcp.sdk.service.cache.communication.CacheStatisticsMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.cache.CacheManager;
import javax.cache.management.CacheStatisticsMXBean;
import javax.management.MBeanServer;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
@Slf4j
public class JSR107CacheHandler implements CacheHandler {

  private final CacheManager cacheManager;
  private final App2WCPConsoleCommunicationService app2WCPConsoleCommunicationService;

  @Override
  public void reload(String cacheName) {
    cacheManager.getCache(cacheName).clear();
  }

  public List<CacheStatisticsMessage> getCacheStatistics() {
    try {
      MBeanServer beanServer = ManagementFactory.getPlatformMBeanServer();
      Set<ObjectInstance> cacheBeans =
          beanServer.queryMBeans(
              ObjectName.getInstance("javax.cache:type=CacheStatistics,CacheManager=*,Cache=*"),
              null);
      List<CacheStatisticsMessage> cacheStatisticsMessages = new ArrayList<>();
      for (ObjectInstance cacheBean : cacheBeans) {
        CacheStatisticsMXBean cacheStatisticsMXBean =
            MBeanServerInvocationHandler.newProxyInstance(
                beanServer, cacheBean.getObjectName(), CacheStatisticsMXBean.class, false);
        String cacheName = cacheBean.getObjectName().getKeyProperty("Cache");

        cacheStatisticsMessages.add(
          CacheStatisticsMessage.builder()
              .name(cacheName)
              .timestamp(Instant.now().toEpochMilli())
              .cacheGets(cacheStatisticsMXBean.getCacheGets())
              .cacheHits(cacheStatisticsMXBean.getCacheHits())
              .cacheMisses(cacheStatisticsMXBean.getCacheMisses())
              .cacheRemovals(cacheStatisticsMXBean.getCacheRemovals())
              .cacheEvictions(cacheStatisticsMXBean.getCacheEvictions())
              .averageGetTime(cacheStatisticsMXBean.getAverageGetTime())
              .averagePutTime(cacheStatisticsMXBean.getAveragePutTime())
              .averageRemoveTime(cacheStatisticsMXBean.getAverageRemoveTime())
              .cacheHitPercentage(cacheStatisticsMXBean.getCacheHitPercentage())
              .cacheMissPercentage(cacheStatisticsMXBean.getCacheMissPercentage())
              .build());
      }
      return cacheStatisticsMessages;
    } catch (Exception e) {
      log.error("Error in getting cache statistics.");
    }
    return Collections.emptyList();
  }

  @Override
  public void resetStats(String cache) {
    try {
      MBeanServer beanServer = ManagementFactory.getPlatformMBeanServer();
      Set<ObjectInstance> cacheBeans =
          beanServer.queryMBeans(
              ObjectName.getInstance("javax.cache:type=CacheStatistics,CacheManager=*,Cache=*"),
              null);
      for (ObjectInstance cacheBean : cacheBeans) {
        CacheStatisticsMXBean cacheStatisticsMXBean =
            MBeanServerInvocationHandler.newProxyInstance(
                beanServer, cacheBean.getObjectName(), CacheStatisticsMXBean.class, false);

        cacheStatisticsMXBean.clear();
      }
    } catch (Exception e) {
      log.error("Error resetting cache statistics.");
    }
  }
}
