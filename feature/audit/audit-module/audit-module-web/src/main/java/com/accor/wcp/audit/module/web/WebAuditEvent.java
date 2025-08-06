package com.accor.wcp.audit.module.web;

import com.accor.wcp.audit.AbstractAuditEvent;
import com.accor.wcp.audit.module.web.request.HttpAuditedRequest;
import com.accor.wcp.audit.module.web.response.HttpAuditedResponse;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Data
public class WebAuditEvent extends AbstractAuditEvent {
  private HttpAuditedRequest httpAuditedRequest;
  private HttpAuditedResponse httpAuditedResponse;
  private String traceId;
  private String origin;
}
