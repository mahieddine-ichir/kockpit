package org.kockpit.communication;

public interface Consumer {

    Message poll(String domain, String env, String appId, String type);
}
