package org.kockpit.features.dynaconfig.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
public class DynaConfigBeanProcessor implements BeanPostProcessor {

    private final Map<String, List<DynaConfigTargetMetadata>> registry = new ConcurrentHashMap<>();

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();
        if (!beanClass.isAnnotationPresent(DynaConfigEnabler.class)) {
            return bean;
        }
        for (Field field : beanClass.getDeclaredFields()) {
            DynaConfigAttribute attribute = field.getAnnotation(DynaConfigAttribute.class);
            if (attribute == null) continue;
            String propertyName = attribute.value().isEmpty() ? field.getName() : attribute.value();
            field.setAccessible(true);
            registry.computeIfAbsent(propertyName, k -> new CopyOnWriteArrayList<>())
                    .add(new DynaConfigTargetMetadata(propertyName, field, bean));
            log.debug("DynaConfig: registered {}.{} for key '{}'",
                    beanClass.getSimpleName(), field.getName(), propertyName);
        }
        return bean;
    }

    List<DynaConfigTargetMetadata> getTargets(String propertyName) {
        return registry.getOrDefault(propertyName, Collections.emptyList());
    }
}