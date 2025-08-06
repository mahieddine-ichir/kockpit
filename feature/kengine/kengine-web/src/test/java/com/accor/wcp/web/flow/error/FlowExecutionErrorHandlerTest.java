package com.accor.wcp.web.flow.error;

import com.accor.wcp.flow.errors.FlowExecutionError;
import com.accor.wcp.flow.errors.FlowExecutionInterruptWarning;
import com.accor.wcp.flow.errors.WcpError;
import jakarta.servlet.http.HttpServletRequest;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;

class FlowExecutionErrorHandlerTest {

  FlowExecutionErrorHandler underTest;
  HttpServletRequest mockedRequest = Mockito.mock(HttpServletRequest.class);

  @BeforeEach
  void init() {
    underTest = new FlowExecutionErrorHandler();
  }

  @Test
  void should_return_error_on_flow_execution_error() {
    FlowExecutionError flowExecutionError = new FlowExecutionError(WcpError.TECHNICAL_ERROR);
    ResponseEntity<ProblemDetail> problemResponse =
        underTest.onFlowExecutionError(flowExecutionError);

    AssertionsForClassTypes.assertThat(problemResponse.getStatusCode())
        .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    AssertionsForClassTypes.assertThat(problemResponse.getBody().getDetail())
        .isEqualTo("Unable to process data. Please contact Welcome Connect team.");
  }

  @Test
  void should_return_no_content_on_flow_execution_interrupt_warning() {
    FlowExecutionInterruptWarning flowExecutionInterruptWarning =
        new FlowExecutionInterruptWarning(WcpError.TECHNICAL_ERROR);
    ResponseEntity<ProblemDetail> problemResponse =
        underTest.onFlowExecutionInterruptWarning(flowExecutionInterruptWarning);

    AssertionsForClassTypes.assertThat(problemResponse.getStatusCode())
        .isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    AssertionsForClassTypes.assertThat(problemResponse.getBody().getDetail())
        .isEqualTo("Unable to process data. Please contact Welcome Connect team.");
  }
}
