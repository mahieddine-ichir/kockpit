package com.accor.wcp.web.rest.config.localresolver.integrationtest;

import static org.assertj.core.api.Assertions.assertThat;

import com.accor.wcp.web.rest.config.localresolver.ReferentialAcceptHeaderLocaleResolver;
import java.util.Arrays;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.servlet.LocaleResolver;

@SpringBootTest(classes = SpringBoot4IntegrationTestApplication.class)
@ActiveProfiles("integrationtest")
@AutoConfigureMockMvc
@Import(TestLocalResolverConfigurationWithCustomLocalReferential.class)
class LocalResolverCustomLocalReferentialIntegrationTest {

  @Autowired private LocaleResolver localeResolver;

  private MockHttpServletRequest httpServletRequest;
  private MockHttpServletResponse httpServletResponse;

  @BeforeEach
  void setUp() {
    httpServletResponse = new MockHttpServletResponse();
    httpServletRequest = new MockHttpServletRequest();
  }


  @Test
  void should_return_localresolver_with_customLocalReferential() throws Exception {

    assertThat(localeResolver).isInstanceOf(ReferentialAcceptHeaderLocaleResolver.class);
    ReferentialAcceptHeaderLocaleResolver referentialAcceptHeaderLocaleResolver = (ReferentialAcceptHeaderLocaleResolver) localeResolver;
    assertThat(referentialAcceptHeaderLocaleResolver.getSupportedLocales()).isEqualTo(
        Arrays.asList(Locale.GERMAN, Locale.CANADA));
    assertThat(referentialAcceptHeaderLocaleResolver.resolveLocale(httpServletRequest)).isEqualTo(Locale.CANADA);
  }

  @Test
  void should_resolveLocale_when_header_accept_language_is_set() throws Exception {

    httpServletRequest.addHeader("Accept-Language", Locale.GERMAN);

    ReferentialAcceptHeaderLocaleResolver referentialAcceptHeaderLocaleResolver = (ReferentialAcceptHeaderLocaleResolver) localeResolver;
    assertThat(referentialAcceptHeaderLocaleResolver.resolveLocale(httpServletRequest)).isEqualTo(Locale.GERMAN);
  }


}
