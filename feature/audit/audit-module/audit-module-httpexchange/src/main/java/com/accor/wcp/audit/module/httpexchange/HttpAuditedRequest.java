package com.accor.wcp.audit.module.httpexchange;

import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpHeaders;

@Data
@Builder
public class HttpAuditedRequest {

  private String uri;

  private HttpHeaders headers;

  private String body;

  private String method;

  private Map<String, List<String>> params;
}
