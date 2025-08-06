package com.accor.wcp.audit.module.httpexchange.obfuscator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class AuditedResponseTooLargeObfuscatorTest {

  @Test
  public void should_return_null_when_body_is_null() {
    AuditedResponseTooLargeObfuscator obfuscator = new AuditedResponseTooLargeObfuscator();

    String body = obfuscator.obfuscateBody(null, null);

    assertNull(body);
  }

  @Test
  public void should_return_body_when_body_is_not_too_large() {
    AuditedResponseTooLargeObfuscator obfuscator = new AuditedResponseTooLargeObfuscator();

    String body = obfuscator.obfuscateBody(null, "body");

    assertThat(body).isEqualTo("body");
  }

  @Test
  public void should_return_null_when_body_is_too_large() {
    AuditedResponseTooLargeObfuscator obfuscator = new AuditedResponseTooLargeObfuscator();
    byte[] stringBytes = new byte[308224];

    String body = obfuscator.obfuscateBody(null, new String(stringBytes));

    assertThat(body)
        .contains(
            "Response has not been audited because payload size (301 KB) exceeded max allowed size 300 KB");
  }
}
