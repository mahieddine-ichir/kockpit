package com.accor.wcp.sdk.command.manager;

/**
 * Convert input context to command identifier (String).
 *
 * @param <T> type of message
 */
public interface CommandTypeAccessor<T> {
  String getCommandType(T context);
}
