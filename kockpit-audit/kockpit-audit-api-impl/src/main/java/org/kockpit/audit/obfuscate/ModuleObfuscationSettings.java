package org.kockpit.audit.obfuscate;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class ModuleObfuscationSettings {
  private String id;

  private List<ObfuscationFilter> filters = new ArrayList<>();

  private Map<String, AuditPropertyObfuscationSettings> properties = new HashMap<>();
}
