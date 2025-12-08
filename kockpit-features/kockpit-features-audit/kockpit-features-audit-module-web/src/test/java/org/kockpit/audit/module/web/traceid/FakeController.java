package org.kockpit.audit.module.web.traceid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
class FakeController {

  @GetMapping("/test")
  public ResponseEntity<String> getTest(@RequestHeader(value = "X-B3-TraceId", required = false) String xB3TraceId) {
    return ResponseEntity.ok("OK-" + xB3TraceId);
  }
}
