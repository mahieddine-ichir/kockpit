package com.accor.wcp.sample.config;

import static com.accor.wcp.sample.sampleflow.flow.context.SampleInfo.SAMPLES_INFO_CACHE;

import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.jsr107.Eh107Configuration;
import org.springframework.boot.autoconfigure.cache.JCacheManagerCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfiguration {

  private final javax.cache.configuration.Configuration<Object, Object> jcacheConfiguration;

  public CacheConfiguration() {
    Cache.Ehcache ehcache = new Cache().getEhcache();

    jcacheConfiguration =
        Eh107Configuration.fromEhcacheCacheConfiguration(
            CacheConfigurationBuilder.newCacheConfigurationBuilder(
                    Object.class, Object.class, ResourcePoolsBuilder.heap(ehcache.getMaxEntries()))
                .withExpiry(
                    ExpiryPolicyBuilder.timeToLiveExpiration(
                        Duration.ofSeconds(ehcache.getTimeToLiveSeconds())))
                .build());
  }

  @Bean
  public JCacheManagerCustomizer cacheManagerCustomizer() {
    return cm -> createCache(cm, SAMPLES_INFO_CACHE);
  }

  private void createCache(javax.cache.CacheManager cm, String cacheName) {
    javax.cache.Cache<Object, Object> cache = cm.getCache(cacheName);
    if (cache != null) {
      cache.clear();
    } else {
      cm.createCache(cacheName, jcacheConfiguration);
    }
  }

  @Getter
  @Setter
  public static class Cache {

    private final Ehcache ehcache = new Ehcache();

    @Getter
    @Setter
    public static class Ehcache {

      private int timeToLiveSeconds = 3600 * 24; // 1 day
      private long maxEntries = 100;
    }
  }
}
