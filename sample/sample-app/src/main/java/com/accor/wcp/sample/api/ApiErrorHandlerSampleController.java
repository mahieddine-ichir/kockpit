package com.accor.wcp.sample.api;

import static java.util.Collections.emptyMap;

import com.accor.wcp.flow.errors.ErrorCodeImpl;
import com.accor.wcp.flow.errors.FlowExecutionError;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController()
public class ApiErrorHandlerSampleController {

  @GetMapping(value = "/api/error/500/npe", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> technical() {
    throw new NullPointerException("Not exposed !");
  }

  @GetMapping(value = "/api/error/500/business", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> business() {
    throw new FlowExecutionError(
        new ErrorCodeImpl(
            "Title for business error",
            500,
            "Details of business error",
            emptyMap(),
            "BUSINESS_CODE"));
  }

  @PostMapping(value = "/api/error/400/body", produces = MediaType.APPLICATION_JSON_VALUE)
  public String badMissingBody(@RequestBody String body) {
    return body;
  }
}
