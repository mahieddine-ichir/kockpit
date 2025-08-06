package com.accor.wcp.console.services.darkcanary.model;

import lombok.Data;

@Data
public class DarkCanaryEndpoint {

    private String uri;

    private String method;

    private String environment;

    private String targetUri;
}
