package com.accor.wcp.helloworld.audit;

import static com.accor.wcp.audit.IndexedKeyValue.of;
import static java.util.Objects.isNull;

import com.accor.wcp.audit.module.web.RequestAuditor;
import com.accor.wcp.audit.module.web.WebAuditReportData;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;

@Component
public class RequestAttributesRequestAuditor implements RequestAuditor {

    private final List<String> requestAttributesToAudit;

    public RequestAttributesRequestAuditor(@Value("${wcp.sdk.service.audit.request-attributes:}") List<String> requestAttributesToAudit) {
        this.requestAttributesToAudit = requestAttributesToAudit;
    }

    @Override
    public void audit(ContentCachingRequestWrapper request, WebAuditReportData webAuditReport) {
        if (isNull(requestAttributesToAudit)) {
            return;
        }
        requestAttributesToAudit.forEach(attributeName -> webAuditReport
                .addIndexedKeyValue(of(attributeName, request.getAttribute(attributeName))));
    }
}
