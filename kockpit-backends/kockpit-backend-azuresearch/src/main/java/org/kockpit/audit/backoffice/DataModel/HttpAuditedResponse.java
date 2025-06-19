package org.kockpit.audit.backoffice.DataModel;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpHeaders;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HttpAuditedResponse {

  private HttpHeaders headers;

  private String payload;

  private int status;
}
