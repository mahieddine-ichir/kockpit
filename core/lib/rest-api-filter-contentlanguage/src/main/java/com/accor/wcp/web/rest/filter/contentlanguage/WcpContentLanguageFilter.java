package com.accor.wcp.web.rest.filter.contentlanguage;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Locale;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.LocaleResolver;

@Component
@Data
@RequiredArgsConstructor
class WcpContentLanguageFilter extends OncePerRequestFilter {
  private final LocaleResolver localeResolver;

  @Override
  protected void doFilterInternal(
      HttpServletRequest httpServletRequest,
      HttpServletResponse httpServletResponse,
      FilterChain filterChain)
      throws ServletException, IOException {
    Locale locale = localeResolver.resolveLocale(httpServletRequest);
    httpServletResponse.addHeader(HttpHeaders.CONTENT_LANGUAGE, locale.toLanguageTag());
    filterChain.doFilter(httpServletRequest, httpServletResponse);
  }
}
