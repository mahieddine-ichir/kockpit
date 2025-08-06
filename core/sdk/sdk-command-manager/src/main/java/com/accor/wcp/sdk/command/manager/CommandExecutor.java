package com.accor.wcp.sdk.command.manager;

/**
 * Command executor generic definition.
 *
 * @param <T>
 */
@FunctionalInterface
public interface CommandExecutor<T> {
  void execute(T notification);
}
