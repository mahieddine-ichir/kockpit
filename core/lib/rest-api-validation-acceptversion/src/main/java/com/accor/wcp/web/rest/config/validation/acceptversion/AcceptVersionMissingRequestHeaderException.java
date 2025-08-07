package com.accor.wcp.web.rest.config.validation.acceptversion;

import org.springframework.web.bind.MissingRequestHeaderException;

class AcceptVersionMissingRequestHeaderException extends MissingRequestHeaderException {

  AcceptVersionMissingRequestHeaderException(String headerName) {
    super(headerName, null);
  }

  @Override
  public String getMessage() {
    return "Missing accept version header in request";
  }
}
