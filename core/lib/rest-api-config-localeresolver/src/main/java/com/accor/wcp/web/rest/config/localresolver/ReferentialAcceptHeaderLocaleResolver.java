package com.accor.wcp.web.rest.config.localresolver;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Locale;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

public class ReferentialAcceptHeaderLocaleResolver extends AcceptHeaderLocaleResolver {

  @Override
  public Locale resolveLocale(HttpServletRequest request) {
    Locale defaultLocale = getDefaultLocale();
    if (defaultLocale != null && StringUtils.isBlank(request.getHeader("Accept-Language"))) {
      return defaultLocale;
    }
    return super.resolveLocale(request);
  }
}
