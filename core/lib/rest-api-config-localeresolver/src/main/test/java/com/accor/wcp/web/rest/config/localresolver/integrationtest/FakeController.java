package com.accor.wcp.web.rest.config.localresolver.integrationtest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
class FakeController {

  @GetMapping("/test")
  public ResponseEntity<String> getTest() {
    return ResponseEntity.ok("OK");
  }
}
