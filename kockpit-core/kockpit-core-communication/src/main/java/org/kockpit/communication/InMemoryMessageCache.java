package org.kockpit.communication;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class InMemoryMessageCache implements MessageCache {

    private final ConcurrentHashMap<String, Message> cache = new ConcurrentHashMap<>();

    @Override
    public void add(Message message) {
        cache.put(message.getId(), message);
    }

    @Override
    public Optional<Message> get(String type, String id) {
        return Optional.ofNullable(cache.get(id));
    }

    @Override
    public List<Message> get(String type) {
        return cache.values().stream().filter(m -> m.getType().equals(type)).toList();
    }
}
