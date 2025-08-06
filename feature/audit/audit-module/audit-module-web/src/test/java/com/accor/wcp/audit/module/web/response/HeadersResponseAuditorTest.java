package com.accor.wcp.audit.module.web.response;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.when;

import com.accor.wcp.audit.IndexedKeyValue;
import com.accor.wcp.audit.module.web.WebAuditEvent;
import com.accor.wcp.audit.module.web.WebAuditReportData;
import jakarta.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.util.ContentCachingResponseWrapper;

class HeadersResponseAuditorTest {

  @Test
  void should_audit_only_specified_header_names() {
    // Given
    HeadersResponseAuditor underTest =
        new HeadersResponseAuditor(Arrays.asList("header1", "header2"));
    HttpServletResponse httpResponse = Mockito.mock(HttpServletResponse.class);
    when(httpResponse.getHeader("header1")).thenReturn("val1");
    when(httpResponse.getHeader("header2")).thenReturn("val2");

    // When
    ContentCachingResponseWrapper response = new ContentCachingResponseWrapper(httpResponse);
    List<IndexedKeyValue> kvs = new ArrayList<>();
    WebAuditReportData report = new WebAuditReportData(WebAuditEvent.builder().build(), kvs);
    underTest.audit(response, report);

    // Then
    assertThat(kvs).hasSize(2);
  }
}
