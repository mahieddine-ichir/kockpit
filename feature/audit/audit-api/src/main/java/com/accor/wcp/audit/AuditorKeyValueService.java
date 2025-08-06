package com.accor.wcp.audit;

import java.util.List;

public interface AuditorKeyValueService {
  void addIndexedKeyValues(List<IndexedKeyValue> indexedKeyValues);

  void addIndexedKeyValues(AuditIndexedKeyValuesComputeFunction computeFunction);
}
