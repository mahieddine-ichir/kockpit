package com.accor.wcp.web.rest.config.localresolver;

import java.util.List;
import java.util.Locale;

public interface LocalesReferential {

  List<Locale> getSupportedLocales();

  Locale getDefaultLocale();
}
