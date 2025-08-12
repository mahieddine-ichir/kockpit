package com.accor.wcp.services.auditstream.notification.darkcanary.fromremote;

import com.accor.wcp.services.auditstream.notification.darkcanary.config.DarkCanaryEndpoint;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

public record RemoteCallMetadata(
    String url,
    String body,
    String method,
    HttpStatusCode statusCode,
    String errorMessage,
    long callDuration,
    DarkCanaryEndpoint endpoint
) {

    static RemoteCallMetadata error(String url, String method, String errorMessage, DarkCanaryEndpoint endpoint) {
        return new RemoteCallMetadata(url, null, method, HttpStatus.INTERNAL_SERVER_ERROR, errorMessage, -1, endpoint);
    }
}
