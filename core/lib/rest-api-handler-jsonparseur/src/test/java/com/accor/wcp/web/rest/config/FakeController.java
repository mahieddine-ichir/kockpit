package com.accor.wcp.web.rest.config;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
class FakeController {

  @GetMapping("/test")
  public ResponseEntity<Object> getValidBody(@Valid @RequestBody FakeUser user) {
    return ResponseEntity.ok("test valid request body is not blank");
  }
}
