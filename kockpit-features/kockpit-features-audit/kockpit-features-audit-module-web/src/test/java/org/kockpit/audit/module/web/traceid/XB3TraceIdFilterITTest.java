package org.kockpit.audit.module.web.traceid;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

import static org.hamcrest.CoreMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

@SpringBootTest(classes = SpringBoot4IntegrationTestApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("integrationtest")
class XB3TraceIdFilterITTest {

  public static final String X_B_3_TRACE_ID = "X-B3-TraceId";

  @Autowired private MockMvc mockMvc;

  @Test
  void should_return_new_generated_trace_id_in_header_response_when_not_found() throws Exception {

    this.mockMvc
        .perform(MockMvcRequestBuilders.get("/test").accept(MediaType.APPLICATION_JSON))
        .andDo(MockMvcResultHandlers.print())
        .andExpect(header().string(X_B_3_TRACE_ID, is(notNullValue())));
  }

  @Test
  void should_return_given_trace_id_in_header_response_when_found_a_valid_one() throws Exception {

    String headerTraceId = "08c72bc3e31283f15a0553d34a5d6a17";

    this.mockMvc
        .perform(
            MockMvcRequestBuilders.get("/test")
                .header(X_B_3_TRACE_ID, headerTraceId)
                .accept(MediaType.APPLICATION_JSON))
        .andDo(MockMvcResultHandlers.print())
        .andExpect(header().string(X_B_3_TRACE_ID, headerTraceId));
  }

  @Test
  void should_generate_a_new_trace_id_in_header_response_when_found_an_invalid_one()
      throws Exception {

    String headerTraceId = "invalidTraceId";

    this.mockMvc
        .perform(
            MockMvcRequestBuilders.get("/test")
                .header(X_B_3_TRACE_ID, headerTraceId)
                .accept(MediaType.APPLICATION_JSON))
        .andDo(MockMvcResultHandlers.print())
        .andExpect(header().string(X_B_3_TRACE_ID, is(not(headerTraceId))));
  }
}
