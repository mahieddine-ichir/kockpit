package org.kockpit.audit.module.web.request;

import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpHeaders;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class HttpAuditedRequest {

  private String uri;

  private HttpHeaders headers;

  private String body;

  private String method;

  private String principal;

  private Map<String, List<String>> params;
}
