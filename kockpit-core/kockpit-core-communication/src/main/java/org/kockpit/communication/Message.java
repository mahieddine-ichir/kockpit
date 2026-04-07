package org.kockpit.communication;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Message {

    private String id;

    private String type;

    private String domain;

    private String env;

    private String appId;

    private long creationDate;

    private Map<String, Object> headers;

    private Object body;

    private List<KeyValue> keyValues;

    @Deprecated
    public Message(String id, String type, String domain, String env, String appId, Object body) {
        this(id, type, domain, env, appId, System.currentTimeMillis(), Map.of(), body, List.of());
    }

    @Deprecated
    public Message(String id, String type, String domain, String env, String appId, Object body, Map<String, Object> headers) {
        this(id, type, domain, env, appId, System.currentTimeMillis(), headers, body, List.of());
    }
}
