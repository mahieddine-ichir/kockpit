package com.accor.wcp.console.services.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SimpleCorsFilter implements Filter {

  @Value("${cors.allow.origins}")
  private String accessAllowOrigins;

  @Override
  public void doFilter(final ServletRequest req, final ServletResponse res, final FilterChain chain)
      throws IOException, ServletException {
    final HttpServletResponse response = (HttpServletResponse) res;
    final HttpServletRequest request = (HttpServletRequest) req;
    // Quick fix for multiple domains in dev env.
    // In production accessAllowOrigins will only have one
    String accessAllowOriginHeader = accessAllowOrigins;
    if ("AUTO".equals(accessAllowOrigins)) {
      String origin = request.getHeader("origin");
      accessAllowOriginHeader = origin;
      log.warn(
          "BECAREFUL: Dev only, using AUTO accessAllowOrigins property => no security at all authorizing CROSS: {}",
          accessAllowOriginHeader);
    }

    response.setHeader("Access-Control-Allow-Origin", accessAllowOriginHeader);
    response.setHeader("Access-Control-Allow-Methods", "POST, GET, PUT, OPTIONS, DELETE");
    response.setHeader("Access-Control-Max-Age", "3600");
    response.setHeader("Access-Control-Allow-Credentials", "true");
    response.setHeader(
        "Access-Control-Allow-Headers",
        "Content-Type, Access-Control-Allow-Headers, Authorization, X-Requested-With");
    response.setHeader(
        "Access-Control-Expose-Headers",
        "X-Requested-With, WWW-Authenticate, Authorization, Origin, Content-Type");

    if (!request.getMethod().equals("OPTIONS")) {
      chain.doFilter(req, res);
    } else {
      response.setStatus(HttpServletResponse.SC_OK);
    }
  }

  @Override
  public void init(final FilterConfig filterConfig) {
    //
  }

  @Override
  public void destroy() {
    //
  }
}
