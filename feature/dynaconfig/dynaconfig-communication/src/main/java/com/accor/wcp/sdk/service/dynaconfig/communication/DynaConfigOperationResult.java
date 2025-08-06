package com.accor.wcp.sdk.service.dynaconfig.communication;

import java.io.Serializable;

public enum DynaConfigOperationResult implements Serializable {
  SENT,
  ACKED,
  DONE,
  ERROR;
}
