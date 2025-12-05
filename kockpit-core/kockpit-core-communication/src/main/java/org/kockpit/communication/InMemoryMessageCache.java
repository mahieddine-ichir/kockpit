package org.kockpit.communication;

import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class InMemoryMessageCache implements MessageCache {

    private final Map<String, Map<String, Message>> cache = new ConcurrentHashMap<>();

    @Override
    public void add(Message message) {
        log.trace("adding message {}", message);
        String key = message.getType();
        cache.putIfAbsent(key, new ConcurrentHashMap<>());
        cache.get(key).put(message.getId(), message);
    }

    @Override
    public Optional<Message> get(String type, String id) {
        if (cache.containsKey(type)) {
            return Optional.ofNullable(cache.get(type).get(id));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Collection<Message> get(String type) {
        if (cache.containsKey(type)) {
            return cache.get(type).values();
        } else {
            return Collections.emptyList();
        }
    }
}
