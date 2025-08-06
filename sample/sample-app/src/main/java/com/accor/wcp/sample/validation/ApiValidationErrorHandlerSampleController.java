package com.accor.wcp.sample.validation;

import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

/** Must be test with the profile [validation] */
@RestController
@Profile("validation")
public class ApiValidationErrorHandlerSampleController {

  @GetMapping(
      value = "/api/error/406/invalid/x-accept-version",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<String> invalidXAcceptVersion(
      @RequestHeader("X-Accept-Version") String xAcceptVersion) {
    return ResponseEntity.ok("X-Accept-Version = " + xAcceptVersion);
  }
}
