package org.kockpit.audit.backend.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpHeaders;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HttpAuditedRequest {

  private String uri;

  private HttpHeaders headers;

  private String body;

  private String method;

  private Map<String, List<String>> params;
}
