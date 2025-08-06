package com.accor.wcp.audit.obfuscate;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@NoArgsConstructor
@ConfigurationProperties(prefix = "wcp.sdk.service.audit.obfuscation")
public class AuditObfuscationSettings {

  List<ModuleObfuscationSettings> configs = new ArrayList<>();
}
