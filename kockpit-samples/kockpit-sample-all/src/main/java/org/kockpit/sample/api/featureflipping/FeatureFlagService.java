package org.kockpit.sample.api.featureflipping;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.kockpit.audit.api.CompressionService;
import org.kockpit.communication.Message;
import org.kockpit.core.sdk.OnMessageListener;
import org.kockpit.features.featureflipping.service.FeatureFlippingDto;
import org.kockpit.features.featureflipping.service.FeatureFlippingServiceDefinition;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FeatureFlagService implements OnMessageListener {

    private final CompressionService compressionService;

    @Setter
    @Getter
    @Value("${compression.enabled:true}")
    private boolean compressionEnabled;

    @Override
    public void onMessage(Message message) {
        if (message.getType().equals(FeatureFlippingServiceDefinition.FEATURE_FLIPPING)) {
            FeatureFlippingDto featureFlippingDto = new ObjectMapper()
                    .convertValue(message.getBody(), FeatureFlippingDto.class);
            if (featureFlippingDto.getKey().equals("compression.enabled")) {
                this.compressionEnabled = featureFlippingDto.getEnabled();
            }
        }
    }
}
