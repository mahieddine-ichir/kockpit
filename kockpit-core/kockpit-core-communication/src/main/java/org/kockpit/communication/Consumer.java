package org.kockpit.communication;

import java.util.List;

public interface Consumer {

    List<Message> poll(String domain, String env, String appId, String type);

    default List<Message> poll(String domain, String env, String type) {
        return poll(domain, env, null, type);
    }
}
