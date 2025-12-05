package org.kockpit.sdk;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kockpit.sdk")
@Data
public class SdkApplicationProperties {

    private String domain;

    private String env;

    private String appId;
}
