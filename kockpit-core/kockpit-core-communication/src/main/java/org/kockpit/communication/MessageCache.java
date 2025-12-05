package org.kockpit.communication;

import java.util.Collection;
import java.util.Optional;

public interface MessageCache {

    void add(Message message);

    Optional<Message> get(String type, String id);

    Collection<Message> get(String type);
}
