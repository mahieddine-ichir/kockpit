package com.accor.wcp.sdk.command.manager;

import static java.util.Objects.isNull;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Generic CommandManager executes the correct command function of input context.
 *
 * @param <T> type of message
 */
@Slf4j
@RequiredArgsConstructor
public class CommandManager<T> {

  private final CommandTypeAccessor<T> commandTypeAccessor;
  private final Map<String, CommandExecutor<T>> commandsMap;

  public void execute(T context) {
    String commandType = commandTypeAccessor.getCommandType(context);
    if (isNull(commandType)) {
      log.debug("No command _type_ defined. Skipping context execution: {}", context);
      return;
    }
    CommandExecutor<T> command = commandsMap.get(commandType);
    if (isNull(command)) {
      log.warn("Command handler not found for : {}", commandType);
      return;
    }
    command.execute(context);
  }
}
