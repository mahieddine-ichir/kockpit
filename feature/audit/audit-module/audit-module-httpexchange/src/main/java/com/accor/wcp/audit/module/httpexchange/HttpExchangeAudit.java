package com.accor.wcp.audit.module.httpexchange;

import com.accor.wcp.audit.AbstractAuditEvent;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

@Data
@SuperBuilder
@Slf4j
public class HttpExchangeAudit extends AbstractAuditEvent {
  public static final String TYPE = "builtin.httpexchanges";

  private HttpAuditedRequest httpAuditedRequest;
  private HttpAuditedResponse httpAuditedResponse;

  public static void logHttpExchange(HttpExchangeAudit event) {
    log.debug(
        "\"method\":\"{}\"," + "\"request\":\"{}\"," + "\"duration\":{}," + "\"httpStatus\":{}",
        event.getHttpAuditedRequest().getMethod(),
        event.getHttpAuditedRequest().getUri(),
        event.getEndTime() - event.getStartTime(),
        event.getHttpAuditedResponse().getStatus());
  }
}
