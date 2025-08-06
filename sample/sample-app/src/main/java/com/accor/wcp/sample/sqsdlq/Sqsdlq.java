package com.accor.wcp.sample.sqsdlq;

import java.util.Map;
import lombok.Data;
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;

@Data
public class Sqsdlq {

    private String body;
    private Map<String, MessageAttributeValue> attributes;

    public Sqsdlq(String body, Map<String, MessageAttributeValue> attributes) {
        this.body = body;
        this.attributes = attributes;
    }
}
