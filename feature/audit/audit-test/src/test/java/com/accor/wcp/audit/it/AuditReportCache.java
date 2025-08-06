package com.accor.wcp.audit.it;

import com.accor.wcp.audit.AuditReport.AuditJsonReport;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections4.CollectionUtils;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AuditReportCache {

  public static final String KEY = "1";
  public static Map<String, AuditJsonReport> cache = new HashMap<>();

  @Before(
      value =
          "execution(* com.accor.wcp.audit.AuditReportNotificationService.notify(..))  && args(auditReport) ")
  public void putAuditJsonReportInCache(List<AuditJsonReport> auditReport) {
    if (CollectionUtils.isNotEmpty(auditReport)) {
      cache.put(KEY, auditReport.get(0));
    }
  }

  public static void clearCache() {
    cache.clear();
  }

  public static AuditJsonReport getReport() {
    return cache.getOrDefault(KEY, null);
  }
}
