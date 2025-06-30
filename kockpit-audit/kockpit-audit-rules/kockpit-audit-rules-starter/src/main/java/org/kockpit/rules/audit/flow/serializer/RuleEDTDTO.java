package org.kockpit.rules.audit.flow.serializer;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RuleEDTDTO extends AbstractEDTDTO {

  private String name;
  private String detail;

}
