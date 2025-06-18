package org.kockpit.audit.obfuscate;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditPathConfig {

  private String path;
  private String mask;

  /**
   * Alias for path.
   *
   * @param key (= path)
   */
  public void setKey(String key) {
    this.path = key;
  }

  /**
   * Alias for path.
   *
   * @return key (= path)
   */
  public String getKey() {
    return path;
  }
}
