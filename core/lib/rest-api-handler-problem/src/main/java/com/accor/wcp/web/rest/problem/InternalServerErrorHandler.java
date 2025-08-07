package com.accor.wcp.web.rest.problem;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class InternalServerErrorHandler {

  private static final String INTERNAL_SERVER_ERROR_TITLE = "Internal Server error";
  private static final String INTERNAL_SERVER_ERROR_CODE = "TECHNICAL_ERROR";

  private static final String KEY_NAME_CODE = "code";

  @Value(
      "${wcp.web.rest.api.handler.error.details.internal-server-error:Unable to process data. Please contact Kockpit Platform Engineering - team.}")
  private String internalServerErrorDetail;

  @ExceptionHandler({Exception.class, RuntimeException.class})
  ResponseEntity<ProblemDetail> allOthers(Throwable t) {
    log.error("Untreated exception, type={}, message={}", t.getClass(), t.getMessage(), new Exception(t));
    ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
    problemDetail.setTitle(INTERNAL_SERVER_ERROR_TITLE);
    problemDetail.setDetail(internalServerErrorDetail);
    problemDetail.setProperty(KEY_NAME_CODE, INTERNAL_SERVER_ERROR_CODE);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problemDetail);
  }
}
