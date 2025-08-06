package com.accor.wcp.audit.module.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Special case for unauthorized request. Filter must have HIGHEST_PRECEDENCE to wrap all Spring
 * filters.
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class SecurityAuditFilter extends OncePerRequestFilter {

  private final AuditFilter auditFilter;

  public SecurityAuditFilter(AuditFilter auditFilter) {
    this.auditFilter = auditFilter;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    filterChain.doFilter(request, response);
    if (401 == response.getStatus()) {
      this.auditFilter.doFilterInternal(request, response, (servletRequest, servletResponse) -> {});
    }
  }
}
