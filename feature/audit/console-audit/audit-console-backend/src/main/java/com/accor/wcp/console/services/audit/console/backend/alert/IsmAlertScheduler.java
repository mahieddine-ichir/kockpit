package com.accor.wcp.console.services.audit.console.backend.alert;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class IsmAlertScheduler {

  private final IsmRequestService esMonitorService;
  private final SnsService snsService;

  @Scheduled(cron = "0 0 0,12 * * *")
  public void checkIsmErrors() {
    try {
      snsService.sendAlertEs(esMonitorService.getManagedIndexOnErrorInfo());
    } catch (Exception e) {
      log.error("Scheduled method checkIsmErrors() encountered error : ", e);
    }
  }
}
