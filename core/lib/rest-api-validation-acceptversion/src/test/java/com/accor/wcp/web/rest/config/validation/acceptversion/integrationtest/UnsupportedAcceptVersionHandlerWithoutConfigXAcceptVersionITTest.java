package com.accor.wcp.web.rest.config.validation.acceptversion.integrationtest;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

@SpringBootTest(classes = SpringBoot4IntegrationTestApplication.class)
@ActiveProfiles("integrationtest-without-x-acceptversion")
@AutoConfigureMockMvc
class UnsupportedAcceptVersionHandlerWithoutConfigXAcceptVersionITTest {

  @Autowired private MockMvc mockMvc;

  @Test
  void should_ignore_handleUnsupportedAcceptVersion_NOT_ACCEPTABLE_when_x_acceptversion_invalid()
      throws Exception {
    MvcResult mvcResult =
        this.mockMvc
            .perform(
                MockMvcRequestBuilders.get("/test")
                    .header("X-Accept-Version", "invalid")
                    .accept(MediaType.APPLICATION_JSON))
            .andDo(MockMvcResultHandlers.print())
            .andReturn();

    assertThat(mvcResult.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }

  @Test
  void should_ignore_handleUnsupportedAcceptVersion_when_BAD_REQUEST_when_x_acceptversion_missing()
      throws Exception {
    MvcResult mvcResult =
        mockMvc
            .perform(MockMvcRequestBuilders.get("/test").accept(MediaType.APPLICATION_JSON))
            .andDo(MockMvcResultHandlers.print())
            .andReturn();

    assertThat(mvcResult.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }
}
