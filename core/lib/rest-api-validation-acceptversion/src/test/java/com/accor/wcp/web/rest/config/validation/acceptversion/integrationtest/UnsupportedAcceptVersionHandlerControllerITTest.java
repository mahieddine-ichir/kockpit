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
@ActiveProfiles("integrationtest")
@AutoConfigureMockMvc
class UnsupportedAcceptVersionHandlerControllerITTest {

  @Autowired private MockMvc mockMvc;

  @Test
  void should_handleUnsupportedAcceptVersion_NOT_ACCEPTABLE_when_x_acceptversion_invalid()
      throws Exception {
    MvcResult mvcResult =
        this.mockMvc
            .perform(
                MockMvcRequestBuilders.get("/test")
                    .header("X-Accept-Version", "invalid")
                    .accept(MediaType.APPLICATION_JSON))
            .andDo(MockMvcResultHandlers.print())
            .andReturn();

    assertThat(mvcResult.getResponse().getStatus()).isEqualTo(HttpStatus.NOT_ACCEPTABLE.value());
    assertThat(mvcResult.getResponse().getContentAsString())
        .isEqualTo(
            "{\"title\":\"Not Acceptable\",\"status\":406,\"detail\":\"Invalid major version passed in header X-Accept-Version (=invalid), accepted version is 1\",\"code\":\"WRONG_VERSION\"}");
  }

  @Test
  void should_handleUnsupportedAcceptVersion_when_BAD_REQUEST_when_x_acceptversion_missing()
      throws Exception {
    MvcResult mvcResult =
        mockMvc
            .perform(MockMvcRequestBuilders.get("/test").accept(MediaType.APPLICATION_JSON))
            .andDo(MockMvcResultHandlers.print())
            .andReturn();

    assertThat(mvcResult.getResponse().getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
    assertThat(mvcResult.getResponse().getErrorMessage())
        .isEqualTo("Required header 'X-Accept-Version' is not present.");
  }

  @Test
  void should_not_handleUnsupportedAcceptVersion_when_x_acceptversion_is_valid() throws Exception {
    MvcResult mvcResult =
        mockMvc
            .perform(
                MockMvcRequestBuilders.get("/test")
                    .header("X-Accept-Version", "1")
                    .accept(MediaType.APPLICATION_JSON))
            .andDo(MockMvcResultHandlers.print())
            .andReturn();

    assertThat(mvcResult.getResponse().getStatus()).isEqualTo(HttpStatus.OK.value());
  }
}
