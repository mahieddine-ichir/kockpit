package com.accor.wcp.web.rest.filter.traceid.integrationtest;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
class FakeController {

  @GetMapping("/test")
  public ResponseEntity<String> getTest(@Parameter(name = "X-B3-TraceId", in = ParameterIn.HEADER) @RequestHeader(value = "X-B3-TraceId", required = false) String xB3TraceId) {
    return ResponseEntity.ok("OK-" + xB3TraceId);
  }
}
