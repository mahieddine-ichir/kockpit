package com.accor.wcp.sample.audit;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
public class AuditResponseHeadersController {

  @GetMapping(value = "/audit/header-responses")
  public ResponseEntity<String> flowCaller(HttpServletResponse response) {
    response.setHeader("response1", "valueHeaderResponse1");
    response.setHeader("response2", "valueHeaderResponse2");
    return ResponseEntity.ok("Audited");
  }
}
