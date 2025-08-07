package com.accor.wcp.web.rest.config.validation.acceptversion;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
class UnsupportedAcceptVersionHandlerController {

  private static final String TITLE = "Not Acceptable";
  private static final String CODE = "WRONG_VERSION";

  @ExceptionHandler(UnsupportedAcceptVersionException.class)
  public ResponseEntity<Object> handleUnsupportedAcceptVersion(
      UnsupportedAcceptVersionException unsupportedAcceptVersionException) {
    ErrorMessage errorMessage =
        ErrorMessage.builder()
            .status(HttpStatus.NOT_ACCEPTABLE.value())
            .title(TITLE)
            .detail(unsupportedAcceptVersionException.getMessage())
            .code(CODE)
            .build();
    return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE)
        .contentType(MediaType.APPLICATION_JSON)
        .body(errorMessage);
  }
}
