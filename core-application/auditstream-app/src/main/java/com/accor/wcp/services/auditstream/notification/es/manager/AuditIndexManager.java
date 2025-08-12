package com.accor.wcp.services.auditstream.notification.es.manager;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public interface AuditIndexManager {

  String getWriteAliasFor(String domain, String env, int ttl) throws IOException;

  Set<String> getAliasWriteAlreadyInit();

  String getAliasRead(String domain, String env);

  void bulkAuditIndexRequests(List<AuditIndexRequest> auditIndexRequests) throws IOException;
}
