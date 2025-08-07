package com.accor.wcp.web.rest.config.localresolver;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

class DefaultLocalesReferential implements LocalesReferential {

  static final Locale DEFAULT_LOCALE = new Locale("en");

  static final List<Locale> SUPPORTED_LOCALES =
      Arrays.asList(
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

  @Override
  public List<Locale> getSupportedLocales() {
    return SUPPORTED_LOCALES;
  }

  @Override
  public Locale getDefaultLocale() {
    return DEFAULT_LOCALE;
  }
}
