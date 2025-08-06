package com.accor.wcp.web.flow.error;

import com.accor.wcp.flow.errors.ErrorCode;
import com.accor.wcp.flow.errors.FlowExecutionError;
import com.accor.wcp.flow.errors.FlowExecutionInterruptWarning;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
public class FlowExecutionErrorHandler {

  private static final String KEY_NAME_CODE = "code";

  @ExceptionHandler({FlowExecutionError.class})
  ResponseEntity<ProblemDetail> onFlowExecutionError(FlowExecutionError ex) {
    return buildResponseEntity(ex.getErrorCode());
  }

  @ExceptionHandler({FlowExecutionInterruptWarning.class})
  ResponseEntity<ProblemDetail> onFlowExecutionInterruptWarning(FlowExecutionInterruptWarning ex) {
    return buildResponseEntity(ex.getErrorCode());
  }

  private ResponseEntity<ProblemDetail> buildResponseEntity(ErrorCode errorCode) {
    ProblemDetail problemDetail = ProblemDetail.forStatus(errorCode.getStatus());
    problemDetail.setDetail(errorCode.getDetail());
    problemDetail.setTitle(errorCode.getTitle());
    problemDetail.setProperty(KEY_NAME_CODE, errorCode.name());

    errorCode.getParameters().forEach(problemDetail::setProperty);

    return new ResponseEntity<>(problemDetail, HttpStatus.valueOf(errorCode.getStatus()));
  }
}
