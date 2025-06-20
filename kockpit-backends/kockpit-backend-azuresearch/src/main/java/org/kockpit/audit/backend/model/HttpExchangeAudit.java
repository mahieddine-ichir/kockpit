package org.kockpit.audit.backend.model;


import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.*;

import java.util.List;
@EqualsAndHashCode(callSuper = true)
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeName("http")
@JsonDeserialize(builder = HttpExchangeAudit.HttpExchangeAuditBuilder.class)
public class HttpExchangeAudit extends AbstractAuditEvent implements Audit {
  public static final String TYPE = "builtin.httpexchanges";

  private HttpAuditedRequest httpAuditedRequest;
  private HttpAuditedResponse httpAuditedResponse;

  @Override
  public String getType() {
    return TYPE;
  }

  @Override
  public List<AuditEvent> getEvents() {
    return List.of();
  }

  @JsonPOJOBuilder(withPrefix = "")
  public static class HttpExchangeAuditBuilder {
  }
}