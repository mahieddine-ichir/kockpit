package org.kockpit.communication.polling;

import lombok.extern.slf4j.Slf4j;
import org.kockpit.communication.Message;
import org.kockpit.communication.MessageCache;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class InMemoryMessageCache implements MessageCache {

    private final ConcurrentHashMap<String, Message> cache = new ConcurrentHashMap<>();

    @Override
    public void add(Message message) {
        log.trace("update/add new message ({}, {})", message.getType(), message);
        cache.put(message.getId(), message);
    }

    @Override
    public Optional<Message> get(String type, String id) {
        return Optional.ofNullable(cache.get(id));
    }
}
