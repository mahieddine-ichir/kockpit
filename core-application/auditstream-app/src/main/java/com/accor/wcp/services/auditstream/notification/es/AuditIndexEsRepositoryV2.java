package com.accor.wcp.services.auditstream.notification.es;

import com.accor.wcp.services.auditstream.notification.es.manager.AuditIndexManager;
import com.accor.wcp.services.auditstream.notification.es.manager.AuditIndexRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuditIndexEsRepositoryV2 {

  private final AuditIndexManager auditIndexEsManager;

  public void bulkIndexRequests(List<AuditIndexRequest> indexRequests) throws IOException {
    auditIndexEsManager.bulkAuditIndexRequests(indexRequests);
  }
}
