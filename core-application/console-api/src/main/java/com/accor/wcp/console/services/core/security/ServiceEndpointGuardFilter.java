package com.accor.wcp.console.services.core.security;

import com.accor.wcp.console.services.core.servicemanager.ServiceManager;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.util.List;

import static com.accor.wcp.console.services.core.security.AuthenticationHelper.isUserGroupsAuthorizedFor;

@Slf4j
@RequiredArgsConstructor
public class ServiceEndpointGuardFilter implements Filter {
  private final ServiceManager serviceManager;

  @Override
  public void doFilter(
      ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
      throws IOException, ServletException {
    HttpServletRequest request = (HttpServletRequest) servletRequest;
    String requestURI = request.getServletPath();
    if (requestURI.startsWith("/api/services")) {
      String[] fragments = requestURI.split("/");
      // Get domain
      String domain = fragments[3];

      // Get env
      if (fragments.length > 4) {
        String env = fragments[4];

        List<String> authenticatedUserGroups = AuthenticationHelper.getAuthenticatedUserGroups();
        if (!isUserGroupsAuthorizedFor(serviceManager, authenticatedUserGroups, domain, env)) {
          HttpServletResponse response = (HttpServletResponse) servletResponse;
          response.sendError(HttpStatus.FORBIDDEN.value());
          return;
        }
      }
    }
    filterChain.doFilter(servletRequest, servletResponse);
  }
}
