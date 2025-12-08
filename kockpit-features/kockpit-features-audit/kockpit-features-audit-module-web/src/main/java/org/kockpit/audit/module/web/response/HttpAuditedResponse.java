package org.kockpit.audit.module.web.response;

import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpHeaders;

@Builder
@Data
public class HttpAuditedResponse {

  private HttpHeaders headers;

  private String body;

  private int status;
}
