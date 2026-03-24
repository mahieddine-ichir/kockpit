package org.kockpit.communication;

public interface Publisher {

    void publish(Message message);

    default void cleanup() {}

}
