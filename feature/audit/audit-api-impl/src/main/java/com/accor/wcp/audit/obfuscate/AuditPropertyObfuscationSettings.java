package com.accor.wcp.audit.obfuscate;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditPropertyObfuscationSettings {

  private String contentType;

  private List<AuditPathConfig> payloadPaths;

  private List<AuditPathConfig> mapKeys;

  private String valueMask;
}
