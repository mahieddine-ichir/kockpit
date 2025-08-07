package com.accor.wcp.web.rest.filter.traceid;

import io.opentelemetry.api.trace.TraceId;
import io.opentelemetry.sdk.trace.IdGenerator;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

class WcpXB3TraceIdManagerFilter extends OncePerRequestFilter {

  private final String traceIdHeaderName;

  public WcpXB3TraceIdManagerFilter(String traceIdHeaderName) {
    this.traceIdHeaderName = traceIdHeaderName;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String traceId =
        getOrGenerateTraceIdAndSetItInRequestAttributeAndResponseHeader(request, response);
    AddParamsToHeader requestWrapper = new AddParamsToHeader(request);
    requestWrapper.setHeader(traceIdHeaderName, traceId);
    filterChain.doFilter(requestWrapper, response);
  }

  private String getOrGenerateTraceIdAndSetItInRequestAttributeAndResponseHeader(
      HttpServletRequest request, HttpServletResponse response) {
    String traceId = getOrGenerateTraceId(request);
    MDC.put("traceId", traceId);
    MDC.put(traceIdHeaderName, traceId);
    request.setAttribute(traceIdHeaderName, traceId);
    response.setHeader(traceIdHeaderName, traceId);
    return traceId;
  }

  private String getOrGenerateTraceId(HttpServletRequest request) {
    String traceId = request.getHeader(traceIdHeaderName);

    if (TraceId.isValid(traceId)) {
      return traceId;
    }
    return IdGenerator.random().generateTraceId();
  }
}
