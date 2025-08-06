package com.accor.wcp.audit.module.web.response;

import com.accor.wcp.audit.module.web.WebAuditEvent;
import com.accor.wcp.audit.module.web.WebAuditReportData;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.util.ArrayList;

class DefaultResponseAuditorTest {

    DefaultResponseAuditor auditor = new DefaultResponseAuditor(false);

    @Test
    void on_large_response() throws IOException {
        WebAuditReportData auditReport = new WebAuditReportData(
                WebAuditEvent.builder().build(),
                new ArrayList<>()
        );

        MockHttpServletResponse response = new MockHttpServletResponse();
        response.setHeader(HttpHeaders.AUTHORIZATION, "Bearer token any");
        byte[] bytes = this.getClass().getResourceAsStream("/response-helloworld.json").readAllBytes();
        response.getWriter().write(new String(bytes));

        ContentCachingResponseWrapper contentCachingResponseWrapper = new ContentCachingResponseWrapper(response);
        auditor.audit(contentCachingResponseWrapper, auditReport);
    }
}