package org.kockpit.communication;

import java.util.Optional;

public interface MessageCache {

    void add(Message message);

    Optional<Message> get(String type, String id);
}
