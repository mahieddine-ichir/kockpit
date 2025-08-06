package com.accor.wcp.audit.it;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ObfuscateWebController {

  @PostMapping(
      value = "/audit/obfuscate/web/json",
      produces = {"application/json"})
  @ResponseBody
  public ResponseEntity<String> obfuscateWebJson(@RequestBody String body) {
    return ResponseEntity.ok(body);
  }

  @PostMapping(
      value = "/audit/obfuscate/web/xml",
      produces = {"application/xml"})
  @ResponseBody
  public ResponseEntity<String> obfuscateWebXml(@RequestBody String body) {
    return ResponseEntity.ok(body);
  }
}
