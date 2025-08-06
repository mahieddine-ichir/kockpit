package com.accor.kengine.audit.model;

import java.util.Date;

public interface CommonExecution {

  String getMessage();

  String getDetail();

  long getTime();

  ResultStatus getResultStatus();

  Date getDate();
}
