package com.accor.wcp.services.auditstream.notification.darkcanary.comparators;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class JsonListReorderer {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @SneakyThrows
    public String order(String json) {
        Map<String, Object> map = objectMapper.readValue(json, Map.class);
        Map<String, Object> copy = copyMap(map);
        return objectMapper.writeValueAsString(copy);
    }

    private Map<String, Object> copyMap(Map<String, Object> map) {
        Map<String, Object> copy = new HashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Object value = entry.getValue();
            String key = entry.getKey();
            if (value instanceof Map submap) {
                copy.put(key, copyMap(submap));
            } else if (value instanceof List list) {
                Collections.sort(list, Comparator.comparingInt(Object::hashCode));
                ArrayList<Object> objects = new ArrayList<>(list.size());
                for (Object object : list) {
                    if (object instanceof Map submap) {
                        objects.add(copyMap(submap));
                    } else {
                        objects.add(object);
                    }
                }
                copy.put(key, objects);
            } else {
                copy.put(key, value);
            }
        }
        return copy;
    }
}
