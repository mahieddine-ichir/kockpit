package org.kockpit.audit.module.web.traceid;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.util.LinkedCaseInsensitiveMap;

import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import static java.util.Objects.nonNull;

class AddParamsToHeader extends HttpServletRequestWrapper {
  private final LinkedCaseInsensitiveMap<String> extendedHeadersMap =
      new LinkedCaseInsensitiveMap<>();

  public AddParamsToHeader(HttpServletRequest request) {
    super(request);
  }

  @Override
  public String getHeader(String name) {
    // first, return the extended value
    String header = extendedHeadersMap.get(name);
    return nonNull(header) ? header : super.getHeader(name);
  }

  void setHeader(String name, String value) {
    extendedHeadersMap.put(name, value);
  }

  @Override
  public Enumeration<String> getHeaderNames() {
    List<String> names = Collections.list(super.getHeaderNames());
    // do not return duplicate header (ignore case)
    names.addAll(
        extendedHeadersMap.keySet().stream()
            .filter(h -> names.stream().noneMatch(h::equalsIgnoreCase))
            .toList());
    return Collections.enumeration(names);
  }

  @Override
  public Enumeration<String> getHeaders(String name) {
    // first, return the extended value
    String headerValue = extendedHeadersMap.get(name);
    return nonNull(headerValue) ? Collections.enumeration(List.of(headerValue)) : super.getHeaders(name);
  }
}
