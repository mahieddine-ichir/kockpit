package org.kockpit.sdk;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@AutoConfiguration
@EnableConfigurationProperties(SdkApplicationProperties.class)
public class SdkAutoConfiguration {
}