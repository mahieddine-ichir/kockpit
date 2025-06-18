package org.kockpit.audit.api;

/** Service definition to set a custom TTL for current audit report. */
public interface AuditorTtlService {
  void setTtl(int ttl);
}
