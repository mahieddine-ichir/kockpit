package org.kockpit.audit.obfuscation.impl;

import org.kockpit.audit.obfuscation.ObfuscateConfig;
import org.kockpit.audit.obfuscation.ObjectObfuscateConfig;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ObjectObfuscateConfigImpl implements ObjectObfuscateConfig {
  Map<String, ObfuscateConfig> obfuscateConfigByProperty;
}
