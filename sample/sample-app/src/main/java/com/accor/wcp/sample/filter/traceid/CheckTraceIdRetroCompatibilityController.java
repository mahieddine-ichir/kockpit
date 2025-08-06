package com.accor.wcp.sample.filter.traceid;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Profile("retrocompatibility")
class CheckTraceIdRetroCompatibilityController {

  static final String WCP_OLD_TRACE_ID = "X-WCP-TraceId";

  @Value(value = "${wcp.web.rest.api.header.traceid.retrocompatibilty}")
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

  @GetMapping(value = "/filter/retrocompatibility/traceId")
  public ResponseEntity<TraceIdInfo> getTraceIdInfo(@RequestHeader Map<String, String> headers) {

    String traceIdName = "";

    if (headers.containsKey(WCP_OLD_TRACE_ID)) {
      traceIdName = WCP_OLD_TRACE_ID;
    }
    TraceIdInfo traceInfo =
        TraceIdInfo.builder()
            .givenOrGeneratedTraceId(headers.get(WCP_OLD_TRACE_ID))
            .traceIdName(traceIdName)
            .retroCompatibilityActivated(retrocompatibilityActivated)
            .build();
    return ResponseEntity.ok(traceInfo);
  }
}
