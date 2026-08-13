package org.kockpit.audit.module.web.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Builder
@Data
public class HttpAuditedResponse {

  private Map<String, List<String>> headers;

  private String body;

  private int status;
}
