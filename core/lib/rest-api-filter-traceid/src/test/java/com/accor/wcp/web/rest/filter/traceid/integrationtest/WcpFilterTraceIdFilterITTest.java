package com.accor.wcp.web.rest.filter.traceid.integrationtest;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

@SpringBootTest(
    properties = {"wcp.web.rest.api.header.traceid.retrocompatibilty=true"},
    classes = SpringBoot4IntegrationTestApplication.class)
@AutoConfigureMockMvc
class WcpFilterTraceIdFilterITTest {

  @Autowired private MockMvc mockMvc;

  @Test
  void should_return_new_generated_trace_id_in_header_response_when_not_found() throws Exception {

    this.mockMvc
        .perform(MockMvcRequestBuilders.get("/test").accept(MediaType.APPLICATION_JSON))
        .andDo(MockMvcResultHandlers.print())
        .andExpect(header().string("X-WCP-TraceId", is(notNullValue())));
  }

  @Test
  void should_return_given_trace_id_in_header_response_when_found() throws Exception {

    String headerTraceId = "headerTraceId";

    this.mockMvc
        .perform(
            MockMvcRequestBuilders.get("/test")
                .header("X-WCP-TraceId", headerTraceId)
                .accept(MediaType.APPLICATION_JSON))
        .andDo(MockMvcResultHandlers.print())
        .andExpect(header().string("X-WCP-TraceId", headerTraceId));
  }
}
