package org.kockpit.communication.legacy.dynaconfig;

import org.kockpit.communication.KeyValue;
import org.kockpit.communication.Message;
import org.kockpit.communication.legacy.LegacyJson;
import org.springframework.util.CollectionUtils;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;

import static java.util.Collections.emptyList;

public class DynaConfigLegacyMapper {

    private final ObjectMapper objectMapper;

    DynaConfigLegacyMapper() {
        this(LegacyJson.mapper());
    }

    DynaConfigLegacyMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    Message toMessage(DynaConfigLegacyMessage legacy) {
        if (legacy == null || legacy.getMessage() == null) {
            return null;
        }
        InstanceInitPropertiesUpdateRequestDto request =
                objectMapper.convertValue(legacy.getMessage(), InstanceInitPropertiesUpdateRequestDto.class);
        return Message.builder()
                .id(legacy.getMessageId())
                .service("dynaconfig")
                .type("update")
                .domain(legacy.getDomain())
                .env(legacy.getEnv())
                .appId(legacy.getApplicationId())
                .creationDate(Instant.now().getEpochSecond())
                .keyValues(toKeyValues(request))
                .build();
    }

    private List<KeyValue> toKeyValues(InstanceInitPropertiesUpdateRequestDto request) {
        if (request == null || CollectionUtils.isEmpty(request.getUpdates())) {
            return emptyList();
        }
        return request.getUpdates().stream()
                .map(u -> new KeyValue(u.getPropertyName(), u.getNewValue()))
                .toList();
    }

    DynaConfigLegacyMessage map(Message message) {
        return switch (message.getType()) {
            case "update" -> forUpdates(message);
            case "ack" -> forAcknowledge(message);
            default -> null;
        };
    }

    private DynaConfigLegacyMessage forUpdates(Message message) {
        return DynaConfigLegacyMessage.builder()
                .applicationId(message.getAppId())
                .domain(message.getDomain())
                .env(message.getEnv())
                .messageId(message.getId())
                .internalType("BROADCAST") // fixme handle INSTANCE (instanceId)
                .serviceId("dynaconfig")
                .message(
                        InstanceInitPropertiesUpdateRequestDto.builder()
                                .updates(toPropertyUpdateMessageRequestList(message))
                                .build()
                )
                .build();
    }

    private DynaConfigLegacyMessage forAcknowledge(Message message) {
        return DynaConfigLegacyMessage.builder()
                .serviceId("dynaconfig")
                .applicationId(message.getAppId()) // fixme
                .domain(message.getDomain())
                .env(message.getEnv())
                //.instanceId(message.getId()) // fixme -> use messageCache?
                .messageId(message.getId())
                .internalType("ACK")
                .build();
    }


    private List<PropertyUpdateMessageRequestDto> toPropertyUpdateMessageRequestList(Message message) {
        if (CollectionUtils.isEmpty(message.getKeyValues())) {
            return emptyList();
        }
        return message.getKeyValues().stream()
                .map(keyValue -> PropertyUpdateMessageRequestDto.builder()
                        .propertyName(keyValue.key())
                        .newValue(toNewValue(keyValue.value()))
                        .build())
                .toList();
    }

    private String toNewValue(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof List<?> list && !list.isEmpty()) {
            if (list.get(0) == null) {
                return null;
            } else {
                return list.get(0).toString();
            }
        } else {
            return value.toString();
        }
    }

}
