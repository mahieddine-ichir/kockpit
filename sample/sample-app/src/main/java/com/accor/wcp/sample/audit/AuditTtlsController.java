package com.accor.wcp.sample.audit;

import com.accor.wcp.audit.AuditKeyConstants;
import com.accor.wcp.audit.AuditorTtlService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequiredArgsConstructor
public class AuditTtlsController {

  private final AuditorTtlService auditorTtlService;

  @GetMapping(value = "/audit/ttl/1")
  public ResponseEntity<String> ttl1(HttpServletRequest request) {
    request.setAttribute(AuditKeyConstants.AUDIT_TTL, 1);
    return ResponseEntity.ok("Ttl 1 day");
  }

  @GetMapping(value = "/audit/ttl/3")
  public ResponseEntity<String> ttl3(HttpServletRequest request) {
    request.setAttribute(AuditKeyConstants.AUDIT_TTL, 3);
    return ResponseEntity.ok("Ttl 3 days");
  }

  @GetMapping(value = "/audit/ttl/10")
  public ResponseEntity<String> ttl10() {
    auditorTtlService.setTtl(10);
    return ResponseEntity.ok("Ttl 10 days");
  }

  @GetMapping(value = "/audit/ttl/30")
  public ResponseEntity<String> ttl30() {
    auditorTtlService.setTtl(30);
    return ResponseEntity.ok("Ttl 30 days");
  }

  @GetMapping(value = "/audit/ttl/90")
  public ResponseEntity<String> ttl90() {
    auditorTtlService.setTtl(90);
    return ResponseEntity.ok("Ttl 90 days");
  }
}
