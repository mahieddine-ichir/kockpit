package org.kockpit.audit.obfuscate;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@ConfigurationProperties(prefix = "kockpit.audit.obfuscation")
public class AuditObfuscationSettings {

  List<ModuleObfuscationSettings> configs = new ArrayList<>();
}
