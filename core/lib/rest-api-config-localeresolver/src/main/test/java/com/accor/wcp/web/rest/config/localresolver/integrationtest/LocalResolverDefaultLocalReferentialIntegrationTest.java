package com.accor.wcp.web.rest.config.localresolver.integrationtest;

import static org.assertj.core.api.Assertions.assertThat;

import com.accor.wcp.web.rest.config.localresolver.ReferentialAcceptHeaderLocaleResolver;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.servlet.LocaleResolver;

@SpringBootTest(classes = SpringBoot4IntegrationTestApplication.class)
@ActiveProfiles("integrationtest")
@AutoConfigureMockMvc
class LocalResolverDefaultLocalReferentialIntegrationTest {

  @Autowired private LocaleResolver localeResolver;

  private MockHttpServletRequest httpServletRequest;
  private MockHttpServletResponse httpServletResponse;

  private static final Locale DEFAULT_LOCALE = new Locale("en");

  static final List<Locale> DEFAULT_SUPPORTED_LOCALES =
      List.of(
          new Locale("pt", "BR"),
          new Locale("th"),
          new Locale("ko"),
          new Locale("ar"),
          new Locale("tr"),
          new Locale("it"),
          new Locale("de"),
          new Locale("zh"),
          new Locale("pl"),
          new Locale("sv"),
          new Locale("en"),
          new Locale("ru"),
          new Locale("fr"),
          new Locale("nl"),
          new Locale("es"),
          new Locale("ja"),
          new Locale("pt"),
          new Locale("vi"),
          new Locale("id"));

  @BeforeEach
  void setUp() {
    httpServletResponse = new MockHttpServletResponse();
    httpServletRequest = new MockHttpServletRequest();
  }

  @Test
  void should_return_localresolver_with_customLocalReferential() throws Exception {

    assertThat(localeResolver).isInstanceOf(ReferentialAcceptHeaderLocaleResolver.class);
    ReferentialAcceptHeaderLocaleResolver referentialAcceptHeaderLocaleResolver =
        (ReferentialAcceptHeaderLocaleResolver) localeResolver;
    assertThat(referentialAcceptHeaderLocaleResolver.getSupportedLocales())
        .isEqualTo(DEFAULT_SUPPORTED_LOCALES);
    assertThat(referentialAcceptHeaderLocaleResolver.resolveLocale(httpServletRequest))
        .isEqualTo(DEFAULT_LOCALE);
  }
}
