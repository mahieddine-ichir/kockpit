package com.accor.wcp.console.services.sqsdlq.dynamo;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "Bad Input")
public class BadInputException extends RuntimeException {
  public BadInputException(String message) {
    super(message);
  }
}
