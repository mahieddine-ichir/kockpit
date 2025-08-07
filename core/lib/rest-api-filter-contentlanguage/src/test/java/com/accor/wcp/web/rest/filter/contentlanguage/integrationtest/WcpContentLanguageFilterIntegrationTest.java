package com.accor.wcp.web.rest.filter.contentlanguage.integrationtest;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

@SpringBootTest(classes = SpringBoot4IntegrationTestApplication.class)
@ActiveProfiles("integrationtest")
@AutoConfigureMockMvc
@Import(TestResolverConfiguration.class)
class WcpContentLanguageFilterIntegrationTest {

  private MockHttpServletRequest httpServletRequest;
  private MockHttpServletResponse httpServletResponse;

  @Autowired private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    httpServletResponse = new MockHttpServletResponse();
    httpServletRequest = new MockHttpServletRequest();
  }

  @Test
  void should_return_localresolver_content_language() throws Exception {

    this.mockMvc
        .perform(MockMvcRequestBuilders.get("/test").accept(MediaType.APPLICATION_JSON))
        .andDo(MockMvcResultHandlers.print())
        .andExpect(header().string(HttpHeaders.CONTENT_LANGUAGE, Locale.GERMANY.getLanguage()));
  }
}
