package org.kockpit.core.sdk;

import org.kockpit.communication.Message;

public interface OnMessageListener {

    void onMessage(Message message);
}
