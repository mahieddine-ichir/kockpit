package com.accor.wcp.console.services.core.appmanifest.s3;

import com.accor.wcp.console.services.core.appmanifest.AppManifestRepo;
import com.accor.wcp.console.services.core.appmanifest.s3.datasource.ResourceLoaderS3;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class AppManifestConfig {

  @ConditionalOnMissingBean
  @Bean
  public AppManifestRepo appManifestRepo(ResourceLoaderS3 resourceLoaderS3) {
    return new AppManifestS3Repo(resourceLoaderS3);
  }
}
