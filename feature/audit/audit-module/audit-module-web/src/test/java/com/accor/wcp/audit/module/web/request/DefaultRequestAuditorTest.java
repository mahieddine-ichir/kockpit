package com.accor.wcp.audit.module.web.request;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpHeaders;
import org.springframework.security.web.firewall.RequestRejectedException;

class DefaultRequestAuditorTest {

  @Test
  void should_audit_headers_even_if_bad_value() {
    DefaultRequestAuditor defaultRequestAuditor = new DefaultRequestAuditor("origin", "traceid",
        true);
    HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
    when(request.getHeader("badvalue")).thenThrow(new RequestRejectedException(
        "The request was rejected because the header value -mock- is not allowed."));
    when(request.getHeader("correctvalue")).thenReturn("ok");
    when(request.getHeaderNames()).thenReturn(
        Collections.enumeration(Arrays.asList("badvalue", "correctvalue")));
    HttpHeaders headers = defaultRequestAuditor.getHeaders(request);
    assertThat(headers).isNotNull().hasSize(2);
  }
}
