package org.kockpit.audit.module.web;

import org.kockpit.audit.api.AbstractAuditEvent;
import org.kockpit.audit.module.web.request.HttpAuditedRequest;
import org.kockpit.audit.module.web.response.HttpAuditedResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@Data
public class WebAuditEvent extends AbstractAuditEvent {
  private HttpAuditedRequest httpAuditedRequest;
  private HttpAuditedResponse httpAuditedResponse;
  private String origin;
}
