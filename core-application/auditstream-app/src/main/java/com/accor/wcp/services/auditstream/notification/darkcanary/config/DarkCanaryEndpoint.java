package com.accor.wcp.services.auditstream.notification.darkcanary.config;

import lombok.Data;

@Data
public class DarkCanaryEndpoint {

    private String uri;

    private String method;

    private String environment;

    private String targetUri;
}
