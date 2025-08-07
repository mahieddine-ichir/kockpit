package com.accor.wcp.web.rest.config.validation.acceptversion;

import lombok.AllArgsConstructor;

@AllArgsConstructor
class UnsupportedAcceptVersionException extends Exception {

  private static final String ERROR_MSG =
      "Invalid major version passed in header X-Accept-Version (=%s), accepted version is %s";

  private final String headerAcceptVersion;
  private final String applicationAcceptVersion;

  @Override
  public String getMessage() {
    return String.format(ERROR_MSG, headerAcceptVersion, applicationAcceptVersion);
  }
}
