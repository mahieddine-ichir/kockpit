package org.kockpit.communication;

import java.util.List;

public interface Consumer {

    List<Message> poll(String domain, String env, String appId, String type);
}
