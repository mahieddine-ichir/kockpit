package com.accor.wcp.web.rest.filter.traceid;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Request filter to manage traceId for a request.
 *
 * @deprecated replaced by {@link WcpXB3TraceIdManagerFilter}
 */
@Deprecated
class WcpTraceIdManagerFilter extends OncePerRequestFilter {

  static final String WCP_TRACE_ID = "X-WCP-TraceId";

  private final String traceIdHeaderName;

  public WcpTraceIdManagerFilter(String traceIdHeaderName) {
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
    request.setAttribute(WCP_TRACE_ID, traceId);
    request.setAttribute(traceIdHeaderName, traceId);
    response.setHeader(WCP_TRACE_ID, traceId);
    response.setHeader(traceIdHeaderName, traceId);
    return traceId;
  }

  private String getOrGenerateTraceId(HttpServletRequest request) {
    String traceId = retrieveTraceIdFromHeader(request);
    return (StringUtils.isBlank(traceId)) ? generateUUID() : traceId;
  }

  private String retrieveTraceIdFromHeader(HttpServletRequest request) {
    return request.getHeader(traceIdHeaderName);
  }

  private String generateUUID() {
    return UUID.randomUUID().toString();
  }
}
