package org.kockpit.rules.audit.flow.serializer;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
public class AbstractEDTDTO {

  private String error;
  private String errorMessage;
  private String errorDetails;
  private Date date;
  private long time;

}
