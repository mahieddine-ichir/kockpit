package org.kockpit.communication.legacy.featureflipping;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.kockpit.communication.Message;
import org.springframework.util.CollectionUtils;

public class FeatureFlippingLegacyMapper {

    FeatureFlippingLegacyMessage map(Message message) {
        Object newValue = CollectionUtils.isEmpty(message.getKeyValues())
                ? null
                : message.getKeyValues().get(0).value();

        return FeatureFlippingLegacyMessage.builder()
                .internalType("BROADCAST")
                .messageId(message.getId())
                .serviceId("featureflipping")
                .domain(message.getDomain())
                .env(message.getEnv())
                .applicationId(message.getAppId())
                .message(FeatureFlippingPropertyUpdateRequestDto.builder()
                        .propertyName(message.getId())
                        .newValue(newValue)
                        .build())
                .build();
    }
}