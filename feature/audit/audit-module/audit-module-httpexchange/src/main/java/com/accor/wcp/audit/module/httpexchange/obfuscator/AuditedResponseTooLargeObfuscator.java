package com.accor.wcp.audit.module.httpexchange.obfuscator;

import static org.apache.commons.lang3.StringUtils.isBlank;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import org.springframework.stereotype.Component;

@Component
public class AuditedResponseTooLargeObfuscator implements ResponseBodyAuditObfuscator {

  private static final int MAX_AUDIT_SIZE_KB = 300;
  private static final String PAYLOAD_TOO_LARGE =
      "Response has not been audited because payload size (%.0f KB) exceeded max allowed size %d KB";

  @Override
  public String obfuscateBody(URI uri, String body) {

    if (isBlank(body) || !isAuditedBodyTooLarge(body)) {
      return body;
    } else {
      return String.format(PAYLOAD_TOO_LARGE, this.getPayloadSizeInKB(body), MAX_AUDIT_SIZE_KB);
    }
  }

  private double getPayloadSizeInKB(String body) {
    int length = body.getBytes(StandardCharsets.UTF_8).length;
    return length / 1024.0;
  }

  private boolean isAuditedBodyTooLarge(String body) {
    return getPayloadSizeInKB(body) > MAX_AUDIT_SIZE_KB;
  }
}
