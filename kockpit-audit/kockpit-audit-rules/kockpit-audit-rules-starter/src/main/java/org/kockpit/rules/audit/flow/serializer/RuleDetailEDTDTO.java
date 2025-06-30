package org.kockpit.rules.audit.flow.serializer;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class RuleDetailEDTDTO extends AbstractEDTDTO {

  private String actionPredicate;
  private boolean condition;
  private String name;
  private String detail;

}
