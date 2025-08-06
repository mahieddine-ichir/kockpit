package com.accor.wcp.audit.obfuscate;

import com.accor.wcp.audit.AbstractAuditEvent;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public class FakeAuditEvent extends AbstractAuditEvent {

  private String content;

  private HttpAuditedRequest request;

  private HttpAuditedRequest httpAuditedResponse;
}
