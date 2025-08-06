package com.accor.wcp.sample.obfuscate.config;

import com.accor.wcp.audit.ObfuscateAuditFieldService;
import org.springframework.stereotype.Component;

@Deprecated
@Component
public class ObfuscateAuditFieldServiceImpl implements ObfuscateAuditFieldService {
  @Override
  public String getObfuscateValue(String fieldName, String fieldValue) {
    return "CustomObfuscateValue";
  }
}
