package com.accor.wcp.sample.filter.traceid;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
class CheckTraceIdController {

  static final String WCP_XB3_TRACE_ID = "X-B3-TraceId";

  @Value(value = "${wcp.web.rest.api.header.traceid.retrocompatibilty:false}")
  private boolean retrocompatibilityActivated;

  @Builder
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  private static class TraceIdInfo {

    private String givenOrGeneratedTraceId;
    private String traceIdName;
    private boolean retroCompatibilityActivated;
  }

  @GetMapping(value = "/filter/xb3traceId")
  public ResponseEntity<TraceIdInfo> getTraceIdInfo(@RequestHeader Map<String, String> headers) {

    String traceIdName = "";

    if (headers.containsKey(WCP_XB3_TRACE_ID)) {
      traceIdName = WCP_XB3_TRACE_ID;
    }

    TraceIdInfo traceInfo =
        TraceIdInfo.builder()
            .givenOrGeneratedTraceId(headers.get(WCP_XB3_TRACE_ID))
            .traceIdName(traceIdName)
            .retroCompatibilityActivated(retrocompatibilityActivated)
            .build();
    return ResponseEntity.ok(traceInfo);
  }
}
