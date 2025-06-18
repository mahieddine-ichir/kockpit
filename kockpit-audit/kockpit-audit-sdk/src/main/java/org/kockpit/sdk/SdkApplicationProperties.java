package org.kockpit.sdk;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "kockpit.sdk")
@Data
public class SdkApplicationProperties {

    private String domain;

    private String env;

    private String appId;
}
