package org.kockpit.kinesis.s3;

import lombok.extern.slf4j.Slf4j;
import org.kockpit.communication.KeyValue;
import org.kockpit.communication.Message;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Objects.isNull;

@Slf4j
public class KinesisRecordMapper {

    public Message toMessage(KinesisRecord record) {
        return Message.builder()
                .id(nvl(record.getMessageId(), UUID.randomUUID().toString()))
                .service(resolveServiceId(record))
                .domain(record.getDomain())
                .env(record.getEnv())
                .appId(record.getApplicationId())
                .creationDate(System.currentTimeMillis())
                .body(resolveBody(record))
                .keyValues(toKeyValues(extractPropertyValues(record)))
                .build();
    }

    private Object resolveBody(KinesisRecord record) {
        if (record.getApplicationInstance() != null) {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("instanceId", record.getApplicationInstance());
            body.put("appId", record.getApplicationId());
            body.put("ended", record.getEnded());
            return body;
        }
        return record.getMessage();
    }

    String resolveServiceId(KinesisRecord record) {
        if (record.getApplicationInstance() != null) {
            return "heartbeat";
        }
        return resolveServiceId(record.getServiceId());
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> extractPropertyValues(KinesisRecord record) {
        if (record.getPropertyValues() != null) {
            return record.getPropertyValues();
        }
        if (record.getMessage() instanceof Map<?, ?> messageMap) {
            Object nested = messageMap.get("propertyValues");
            //log.trace("Extracted propertyValues from message body: type={}, value={}", nested == null ? "null" : nested.getClass().getSimpleName(), nested);
            if (nested instanceof Map<?, ?>) {
                return (Map<String, Object>) nested;
            }
        }
        return null;
    }

    String resolveServiceId(String serviceId) {
        if (isNull(serviceId)) {
            return null;
        }
        return switch (serviceId) {
            case "dynaconfig" -> "dyna-config";
            case "featureflipping" -> "feature-flipping";
            default -> serviceId;
        };
    }

    private List<KeyValue> toKeyValues(Map<String, Object> propertyValues) {
        if (propertyValues == null) {
            return null;
        }
        return propertyValues.entrySet().stream()
                .map(e -> new KeyValue(e.getKey(), e.getValue()))
                .toList();
    }

    private String nvl(String value, String fallback) {
        return (value != null && !value.isBlank()) ? value : fallback;
    }
}
