package com.accor.wcp.audit.module.httpexchange;

import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpHeaders;

@Builder
@Data
public class HttpAuditedResponse {

  private HttpHeaders headers;

  private String payload;

  private int status;
}
