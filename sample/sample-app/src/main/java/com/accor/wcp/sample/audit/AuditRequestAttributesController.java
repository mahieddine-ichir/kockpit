package com.accor.wcp.sample.audit;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Date;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
public class AuditRequestAttributesController {

  @GetMapping(value = "/audit/request-attributes")
  public ResponseEntity<String> flowCaller(HttpServletRequest request) {
    request.setAttribute("doubleAttribute", Math.random());
    request.setAttribute("floatAttribute", 48593.49493993f);
    request.setAttribute("integerAttribute", 384739);
    request.setAttribute("stringAttribute", "Hello Audit !");
    request.setAttribute("objectAttribute", request.getParameterMap());
    request.setAttribute("dateAttribute", new Date());

    return ResponseEntity.ok("Audited");
  }
}
