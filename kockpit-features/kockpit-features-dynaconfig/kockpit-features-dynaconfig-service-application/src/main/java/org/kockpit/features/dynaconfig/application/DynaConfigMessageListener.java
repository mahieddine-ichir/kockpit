package org.kockpit.features.dynaconfig.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kockpit.communication.Message;
import org.kockpit.core.sdk.OnMessageListener;
import org.kockpit.features.dynaconfig.service.DynaConfigDto;
import org.kockpit.features.dynaconfig.service.DynaConfigServiceDefinition;
import org.springframework.core.convert.ConversionService;
import org.springframework.lang.Nullable;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class DynaConfigMessageListener implements OnMessageListener {

    private final DynaConfigBeanProcessor beanProcessor;
    private final ObjectMapper objectMapper;
    @Nullable
    private final ConversionService conversionService;

    @Override
    public void onMessage(Message message) {
        if (!DynaConfigServiceDefinition.DYNA_CONFIG.equals(message.getType())) {
            return;
        }
        DynaConfigDto dto = objectMapper.convertValue(message.getBody(), DynaConfigDto.class);
        if (dto == null || dto.getValue() == null) {
            return;
        }
        String key = message.getId();
        List<DynaConfigTargetMetadata> targets = beanProcessor.getTargets(key);
        if (targets.isEmpty()) {
            return;
        }
        for (DynaConfigTargetMetadata target : targets) {
            try {
                Object converted = convert(dto.getValue(), target.getField().getType());
                target.getField().set(target.getTarget(), converted);
                log.info("DynaConfig: updated {}.{} = {}",
                        target.getTarget().getClass().getSimpleName(),
                        target.getField().getName(),
                        converted);
            } catch (Exception e) {
                log.error("DynaConfig: failed to update {}.{}",
                        target.getTarget().getClass().getSimpleName(),
                        target.getField().getName(), e);
            }
        }
    }

    private Object convert(Object value, Class<?> targetType) {
        if (targetType.isInstance(value)) {
            return value;
        }
        if (conversionService != null && conversionService.canConvert(value.getClass(), targetType)) {
            return conversionService.convert(value, targetType);
        }
        return objectMapper.convertValue(value, targetType);
    }
}