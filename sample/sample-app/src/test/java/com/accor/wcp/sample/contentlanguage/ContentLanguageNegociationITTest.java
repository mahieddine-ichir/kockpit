package com.accor.wcp.sample.contentlanguage;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;

import com.accor.wcp.sample.WcpSampleApplicationApp;
import java.util.Locale;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = WcpSampleApplicationApp.class)
@AutoConfigureMockMvc
@ActiveProfiles("integrationtest")
class ContentLanguageNegociationITTest {

  @Autowired private MockMvc mockMvc;

  @Test
  void should_return_in_header_response_default_catalog_referential_content_language()
      throws Exception {

    this.mockMvc
        .perform(MockMvcRequestBuilders.get("/baskets/8888").accept(MediaType.APPLICATION_JSON))
        .andDo(MockMvcResultHandlers.print())
        .andExpect(header().string(HttpHeaders.CONTENT_LANGUAGE, Locale.ENGLISH.getLanguage()));
  }

  @Test
  void should_return_in_header_response_matching_catalog_referential_content_language()
      throws Exception {

    this.mockMvc
        .perform(
            MockMvcRequestBuilders.get("/baskets/8888")
                .header(HttpHeaders.ACCEPT_LANGUAGE, "fr-BE,nl;q=0.25,fr-FR;q=0.5")
                .accept(MediaType.APPLICATION_JSON))
        .andDo(MockMvcResultHandlers.print())
        .andExpect(header().string(HttpHeaders.CONTENT_LANGUAGE, Locale.FRENCH.getLanguage()));
  }
}
