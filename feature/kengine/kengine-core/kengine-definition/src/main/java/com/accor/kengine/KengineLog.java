package com.accor.kengine;

import java.util.Date;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Deprecated
public class KengineLog {
  private String log;
  private String action;
  private Date date;
}
