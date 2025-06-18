package org.kockpit.audit.obfuscate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ObfuscationFilter {
  private String path;
  private String expression;
}
