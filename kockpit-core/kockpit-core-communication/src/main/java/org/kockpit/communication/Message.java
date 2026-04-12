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

    private String service;

    private String type;

    private String domain;

    private String env;

    private String appId;

    private long creationDate;

    private Map<String, Object> headers;

    private Object body;

    private List<KeyValue> keyValues;
}
