package org.kockpit.audit.obfuscate;

import lombok.*;

import java.util.List;

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
