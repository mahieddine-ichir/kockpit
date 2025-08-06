package com.accor.wcp.sample.audit.sqs;

import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;

import java.util.Map;

public class SqsMessage {

    private String payload;
    private Map<String, MessageAttributeValue> attributes;

    public SqsMessage(String payload, Map<String, MessageAttributeValue> attributes) {
        this.payload = payload;
        this.attributes = attributes;
    }

    public String getPayload() {
        return payload;
    }

    public Map<String, MessageAttributeValue> getAttributes() {
        return attributes;
    }
}
