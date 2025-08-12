package com.accor.wcp.services.auditstream.notification.darkcanary.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Endpoint {

    private String uri;

    private String env;

    private String targetUri;

    private String method;

    private int statusCode;

    private String errorMessage;

    private Long duration;
}
