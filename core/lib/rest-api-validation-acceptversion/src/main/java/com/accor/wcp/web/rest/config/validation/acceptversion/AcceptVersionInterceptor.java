package com.accor.wcp.web.rest.config.validation.acceptversion;

import static org.apache.commons.lang3.StringUtils.trim;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@AllArgsConstructor
class AcceptVersionInterceptor implements HandlerInterceptor {

  private String applicationAcceptVersion;

  private String acceptVersionHeaderName;

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
      throws Exception {
    String headerAcceptVersion = request.getHeader(acceptVersionHeaderName);

    if (StringUtils.isBlank(headerAcceptVersion)) {
      throw new AcceptVersionMissingRequestHeaderException(acceptVersionHeaderName);
    }

    if (isHeaderAcceptVersionInvalid(headerAcceptVersion)) {
      throw new UnsupportedAcceptVersionException(headerAcceptVersion, applicationAcceptVersion);
    }
    return true;
  }

  private boolean isHeaderAcceptVersionInvalid(String headerAcceptVersion) {
    return StringUtils.compareIgnoreCase(trim(applicationAcceptVersion), trim(headerAcceptVersion))
        != 0;
  }
}
