package org.kockpit.audit.module.web.traceid;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Enumeration;

import static org.junit.jupiter.api.Assertions.*;

class AddParamsToHeaderTest {

    private static final String CUSTOM_TRACE_ID = "custom-traceid";

    @Test
  void should_get_header_from_original_request() {
    MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();
    String traceId = "traceIdTest";
    String newTraceId = "newTraceIdTest";
    httpServletRequest.addHeader(CUSTOM_TRACE_ID, traceId);
    AddParamsToHeader underTest = new AddParamsToHeader(httpServletRequest);
    underTest.setHeader(CUSTOM_TRACE_ID, newTraceId);
    assertEquals(newTraceId, underTest.getHeader(CUSTOM_TRACE_ID));
  }

  @Test
  void should_get_header_from_extended_map() {
    MockHttpServletRequest httpServletRequest = new MockHttpServletRequest();
    String header1Name = "header1";
    httpServletRequest.addHeader(header1Name, "1");
      String oldTraceIdTest = "oldTraceIdTest";
      httpServletRequest.addHeader("x-b3-traceid", oldTraceIdTest);

    String traceId = "traceIdTest";
    AddParamsToHeader underTest = new AddParamsToHeader(httpServletRequest);
    underTest.setHeader(CUSTOM_TRACE_ID, traceId);

    assertEquals(oldTraceIdTest, underTest.getHeader("x-b3-traceid"));
    assertEquals(traceId, underTest.getHeader(CUSTOM_TRACE_ID));
    Enumeration<String> headerNames = underTest.getHeaderNames();
    assertEquals(header1Name, headerNames.nextElement());
    assertEquals("x-b3-traceid", headerNames.nextElement());
    assertTrue(headerNames.hasMoreElements());
  }
}
