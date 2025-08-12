package com.accor.wcp.services.auditstream.notification;

import lombok.experimental.UtilityClass;
import org.springframework.http.HttpHeaders;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Stream;

import static java.util.Objects.nonNull;

@UtilityClass
public class AuditReportHelper {

    public static final String PROPERTY_BUILT_IN = "builtin.web";
    public static final String PROPERTY_HTTP_AUDITED_RESPONSE = "httpAuditedResponse";
    public static final String PROPERTY_HTTP_AUDITED_REQUEST = "httpAuditedRequest";
    public static final String PROPERTY_EVENTS = "events";
    public static final String PROPERTY_TYPE = "type";
    public static final String PROPERTY_BODY = "body";
    public static final String PROPERTY_HEADERS = "headers";
    public static final String PROPERTY_URI = "uri";
    public static final String PROPERTY_METHOD = "method";
    public static final String PROPERTY_PARAMS = "params";

    public static String readTraceId(AuditReportRequest auditReportRequest) {
        return readBuiltinWebEvents(auditReportRequest)
                .findAny()
                .map(stringObjectMap -> stringObjectMap.get("traceId"))
                .map(Object::toString)
                .orElse(null);
    }

    public static Optional<String> readHeaderValue(AuditReportRequest auditReportRequest, String key) {
        return readBuiltinWebEvents(auditReportRequest)
                .map(o -> o.get(PROPERTY_HTTP_AUDITED_REQUEST))
                .map(o -> ((Map<String, Object>) o).get(PROPERTY_HEADERS))
                .map(o -> (Map<String, Object>) o)
                .map(o -> extractHeaderValue(o, key))
                .findFirst()
                .orElse(Optional.empty());
    }

    private static Optional<String> extractHeaderValue(Map<String, Object> headers, String key) {
        return headers.entrySet().stream()
                .filter(entry -> entry.getKey().equalsIgnoreCase(key))
                .map(Map.Entry::getValue)
                .map(o1 -> {
                    if (o1 instanceof List) {
                        // get first if list
                        return ((List<?>) o1).get(0).toString();
                    } else {
                        return o1.toString();
                    }
                }).findFirst();
    }

    public static Optional<String> readRequestUri(AuditReportRequest auditReportRequest) {
        return readBuiltinWebEvents(auditReportRequest)
                .map(o -> o.get(PROPERTY_HTTP_AUDITED_REQUEST))
                .map(o -> ((Map<String, Object>) o).get(PROPERTY_URI))
                .map(Object::toString)
                .findFirst();
    }

    public static Optional<Long> readCallDuration(AuditReportRequest auditReportRequest) {
        return auditReportRequest.getIndexedKeyValues()
                .stream()
                .map(map -> {
                    if (map.get("key") != null && map.get("key").equals("duration")) {
                        return map.get("value");
                    } else {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .map(o -> Long.valueOf(o.toString()))
                .findFirst();
    }

    public static Optional<String> readRequestMethod(AuditReportRequest auditReportRequest) {
        return readBuiltinWebEvents(auditReportRequest)
                .map(o -> o.get(PROPERTY_HTTP_AUDITED_REQUEST))
                .map(o -> ((Map<String, Object>) o).get(PROPERTY_METHOD))
                .map(Object::toString)
                .findFirst();
    }

    public static Optional<Object> readRequestBody(AuditReportRequest auditReportRequest) {
        return readBuiltinWebEvents(auditReportRequest)
                .map(o -> o.get(PROPERTY_HTTP_AUDITED_REQUEST))
                .map(o -> ((Map<String, Object>) o).get(PROPERTY_BODY))
                .findFirst();
    }

    public static Optional<String> readResponse(AuditReportRequest auditReportRequest) {
        return readBuiltinWebEvents(auditReportRequest)
                .map(o -> (Map<String, Object>) o.get(PROPERTY_HTTP_AUDITED_RESPONSE))
                .map(o -> o.get(PROPERTY_BODY))
                .map(Object::toString)
                .findFirst();
    }

    private static Stream<Map<String, Object>> readBuiltinWebEvents(AuditReportRequest auditReportRequest) {
        if (CollectionUtils.isEmpty(auditReportRequest.getAudits())) {
            return Stream.empty();
        }
        return auditReportRequest.getAudits().stream()
                .filter(stringObjectMap -> stringObjectMap.get(PROPERTY_TYPE).toString().equalsIgnoreCase(PROPERTY_BUILT_IN))
                .map(stringObjectMap -> stringObjectMap.get(PROPERTY_EVENTS))
                .map(o -> (List<Map<String, Object>>) o)
                .flatMap(Collection::stream);
    }

    public static Map<String, Object> readRequestParameters(AuditReportRequest auditReportRequest) {
        return readBuiltinWebEvents(auditReportRequest)
                .map(o -> o.get(PROPERTY_HTTP_AUDITED_REQUEST))
                .map(o -> ((Map<String, Object>) o).get(PROPERTY_PARAMS))
                .map(o -> ((Map<String, Object>) o))
                .findAny()
                .orElse(Collections.emptyMap());
    }

    public static Map<String, Object> readRequestHeaders(AuditReportRequest auditReportRequest) {
        return readBuiltinWebEvents(auditReportRequest)
                .map(o -> o.get(PROPERTY_HTTP_AUDITED_REQUEST))
                .map(o -> ((Map<String, Object>) o).get(PROPERTY_HEADERS))
                .map(o -> ((Map<String, Object>) o))
                .findAny()
                .orElse(Collections.emptyMap());
    }

    public static HttpHeaders buildHttpHeaders(AuditReportRequest auditReportRequest) {
        Map<String, Object> headers = readRequestHeaders(auditReportRequest);
        HttpHeaders httpHeaders = new HttpHeaders();
        headers.forEach((s, o) -> {
            if (o instanceof Collection<?> collection) {
                collection.forEach(o1 -> httpHeaders.add(s, o1.toString()));
            } else {
                httpHeaders.add(s, o.toString());
            }
        });
        return httpHeaders;
    }

    public static int computeTtl(AuditReportRequest auditReportRequest) {
        int ttl = 30;
        if (nonNull(auditReportRequest.getTtl())) {
            // Restrict ttl values to following
            // [1, 3, 5, 10, 15, 30, 60]
            ttl = Stream.of(1, 3, 5, 10, 15, 30, 60)
                    .filter(integer -> (integer - auditReportRequest.getTtl()) >= 0)
                    .findFirst()
                    .orElse(60);
        }

        return ttl;
    }
}
