package com.accor.wcp.obfuscation.impl;

import com.accor.wcp.obfuscation.ObfuscateConfig;
import com.accor.wcp.obfuscation.ObjectObfuscateConfig;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ObjectObfuscateConfigImpl implements ObjectObfuscateConfig {
  Map<String, ObfuscateConfig> obfuscateConfigByProperty;
}
