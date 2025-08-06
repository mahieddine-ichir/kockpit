package com.accor.wcp.audit.module.web.request;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.when;

import com.accor.wcp.audit.IndexedKeyValue;
import com.accor.wcp.audit.module.web.WebAuditEvent;
import com.accor.wcp.audit.module.web.WebAuditReportData;
import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.util.ContentCachingRequestWrapper;

class HeadersRequestAuditorTest {

  @Test
  void should_audit_only_specified_header_names() {
    // Given
    HeadersRequestAuditor underTest =
        new HeadersRequestAuditor(Arrays.asList("header1", "header2"));
    HttpServletRequest httpRequest = Mockito.mock(HttpServletRequest.class);
    when(httpRequest.getHeader("header1")).thenReturn("val1");
    when(httpRequest.getHeader("header2")).thenReturn("val2");

    // When
    ContentCachingRequestWrapper request = new ContentCachingRequestWrapper(httpRequest);
    List<IndexedKeyValue> kvs = new ArrayList<>();
    WebAuditReportData report = new WebAuditReportData(WebAuditEvent.builder().build(), kvs);
    underTest.audit(request, report);

    // Then
    assertThat(kvs).hasSize(2);
  }
}
