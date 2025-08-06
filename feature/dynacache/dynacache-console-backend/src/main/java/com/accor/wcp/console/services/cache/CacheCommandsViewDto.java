package com.accor.wcp.console.services.cache;

import com.accor.wcp.sdk.service.cache.communication.CacheInstanceOperationResult;
import com.accor.wcp.sdk.service.cache.communication.CacheOperationResult;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Data;

@Data
public class CacheCommandsViewDto {

  private List<CacheCommandViewDto> cacheCommands;

  public CacheCommandsViewDto(Map<String, CacheCommand> commands) {
    this.cacheCommands =
        commands.entrySet().stream()
            .map(entry -> new CacheCommandViewDto(entry.getKey(), entry.getValue()))
            .collect(Collectors.toList());
  }

  @Data
  private static class CacheCommandViewDto {

    private String id;
    private long dateTime;
    private Command command;
    private List<CacheInstanceOperationResultView> cacheInstanceOperationResults;

    public CacheCommandViewDto(String id, CacheCommand cacheCommand) {
      this.id = id;
      this.command = cacheCommand.getCommand();
      this.dateTime = cacheCommand.getDateTime();
      this.cacheInstanceOperationResults =
          cacheCommand.getInstanceStatus().entrySet().stream()
              .map(entry -> new CacheInstanceOperationResultView(entry.getKey(), entry.getValue()))
              .collect(Collectors.toList());
    }
  }

  @Data
  private static class CacheInstanceOperationResultView {

    private String instanceId;
    private long dateTime;
    private CacheOperationResult result;

    public CacheInstanceOperationResultView(
        String instanceId, CacheInstanceOperationResult cacheInstanceOperationResult) {
      this.instanceId = instanceId;
      this.dateTime = cacheInstanceOperationResult.getDateTime();
      this.result = cacheInstanceOperationResult.getResult();
    }
  }
}
