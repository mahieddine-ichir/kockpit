package com.accor.wcp.sdk.application.config;

import com.accor.wcp.sdk.application.SdkApplicationProperties;
import com.accor.wcp.sdk.application.SdkConfig;
import com.accor.wcp.sdk.application.SdkEnvironmentConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.InputStream;

@Configuration
public class ApplicationSdkConfigurationPropertiesConfig {

  @Value("${wcp.sdk.config.classpath-file:sdk-config.yml}")
  String sdkConfigurationClasspathFile;

  private SdkConfig loadSdkConfig() throws IOException {
    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    mapper.setPropertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE);
    InputStream resourceAsStream = getClass().getClassLoader()
        .getResourceAsStream(sdkConfigurationClasspathFile);
    return mapper.readValue(
        resourceAsStream,
        SdkConfig.class);
  }

  @Bean
  SdkConfig sdkConfig() throws IOException {
    // Read SDK Configuration file
    return loadSdkConfig();
  }

  @Bean
  SdkEnvironmentConfig sdkEnvironmentConfig(SdkApplicationProperties sdkApplicationProperties,
      SdkConfig sdkConfig) {
    String sdkEnv = sdkApplicationProperties.getWcpEnv();
    if (!sdkConfig.getEnvironments().containsKey(sdkEnv)) {
      throw new IllegalArgumentException("SDK Environment not found. env=" + sdkEnv);
    }
    return sdkConfig.getEnvironments().get(sdkEnv);
  }

  @Bean
  SdkConfigurationAws sdkConfigurationProperties(SdkEnvironmentConfig sdkEnvironmentConfig) {
    return new SdkConfigurationAws(sdkEnvironmentConfig.getAwsAccountId());
  }

}
