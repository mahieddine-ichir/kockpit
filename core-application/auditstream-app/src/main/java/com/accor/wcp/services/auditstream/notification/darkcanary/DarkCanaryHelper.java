package com.accor.wcp.services.auditstream.notification.darkcanary;

import com.accor.wcp.services.auditstream.notification.AuditReportRequest;
import com.accor.wcp.services.auditstream.notification.darkcanary.config.DarkCanaryEndpoint;
import lombok.experimental.UtilityClass;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.accor.wcp.services.auditstream.notification.AuditReportHelper.readRequestParameters;

@UtilityClass
public class DarkCanaryHelper {

    public static String buildUrl(AuditReportRequest auditReportRequest, DarkCanaryEndpoint endpoint) {
        Map<String, Object> params = readRequestParameters(auditReportRequest);
        String queryParams = params.entrySet().stream()
                .filter(stringObjectEntry -> Objects.nonNull(stringObjectEntry.getValue()))
                .map(entry -> {
                    if (entry.getValue() instanceof Collection collection) {
                        Iterator iterator = collection.iterator();
                        if (iterator.hasNext()) {
                            return entry.getKey() + "=" + iterator.next().toString();
                        } else {
                            return null;
                        }
                    } else {
                        return entry.getKey() + "=" + entry.getValue();
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.joining("&"));

        if (StringUtils.hasText(queryParams)) {
            return endpoint.getTargetUri()+"?"+queryParams;
        } else {
            return endpoint.getTargetUri();
        }
    }
}
