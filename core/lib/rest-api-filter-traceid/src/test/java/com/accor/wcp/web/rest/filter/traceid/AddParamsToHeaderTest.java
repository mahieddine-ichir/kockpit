package com.accor.wcp.web.rest.filter.traceid;

import static com.accor.wcp.web.rest.filter.traceid.WcpTraceIdManagerFilter.WCP_TRACE_ID;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Enumeration;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class AddParamsToHeaderTest {

  @Test
  void should_get_header_from_original_request() {
    MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();
    String traceId = "traceIdTest";
    String newTraceId = "newTraceIdTest";
    httpServletRequest.addHeader(WCP_TRACE_ID, traceId);
    AddParamsToHeader underTest = new AddParamsToHeader(httpServletRequest);
    underTest.setHeader(WCP_TRACE_ID, newTraceId);
    assertEquals(newTraceId, underTest.getHeader(WCP_TRACE_ID));
  }

  @Test
  void should_get_header_from_extended_map() {
    MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();
    String header1Name = "header1";
    httpServletRequest.addHeader(header1Name, "1");
    httpServletRequest.addHeader("x-wcp-traceid", "oldTraceIdTest");

    String traceId = "traceIdTest";
    AddParamsToHeader underTest = new AddParamsToHeader(httpServletRequest);
    underTest.setHeader(WCP_TRACE_ID, traceId);

    assertEquals(traceId, underTest.getHeader("x-wcp-traceid"));
    assertEquals(traceId, underTest.getHeader(WCP_TRACE_ID));
    Enumeration<String> headerNames = underTest.getHeaderNames();
    assertEquals(header1Name, headerNames.nextElement());
    assertEquals("x-wcp-traceid", headerNames.nextElement());
    assertFalse(headerNames.hasMoreElements());
  }
}
