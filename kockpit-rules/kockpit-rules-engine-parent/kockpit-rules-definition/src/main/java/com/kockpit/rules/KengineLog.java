package com.kockpit.rules;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
@Deprecated
public class KengineLog {
  private String log;
  private String action;
  private Date date;
}
