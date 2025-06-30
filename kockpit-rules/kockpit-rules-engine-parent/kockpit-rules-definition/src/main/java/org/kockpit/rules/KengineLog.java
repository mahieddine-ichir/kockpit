package org.kockpit.rules;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class KengineLog {
  private String log;
  private String action;
  private Date date;
}
