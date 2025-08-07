package com.accor.wcp.web.rest.config.validation.acceptversion;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

@ExtendWith(MockitoExtension.class)
class AcceptVersionInterceptorTest {

  public static final String X_ACCEPT_VERSION_NAME = "X-Accept-Version";
  public static final String APPLICATION_ACCEPT_VERSION = "2";
  private MockHttpServletRequest httpServletRequest;
  private MockHttpServletResponse httpServletResponse;

  AcceptVersionInterceptor acceptVersionInterceptor = null;

  @BeforeEach
  void setUp() {
    httpServletResponse = new MockHttpServletResponse();
    httpServletRequest = new MockHttpServletRequest();
  }

  @Test
  void checkAcceptVersion_ignore_when_application_version_undefined() throws Exception {

    // GIVEN
    acceptVersionInterceptor = new AcceptVersionInterceptor(null, X_ACCEPT_VERSION_NAME);

    // WHEN
    assertThrows(
        AcceptVersionMissingRequestHeaderException.class,
        () -> acceptVersionInterceptor.preHandle(httpServletRequest, httpServletResponse, "test"));
  }

  @Test
  void checkAcceptVersion_thrown_error_when_application_version_invalid() throws Exception {
    // GIVEN
    acceptVersionInterceptor =
        new AcceptVersionInterceptor(APPLICATION_ACCEPT_VERSION, X_ACCEPT_VERSION_NAME);
    httpServletRequest.addHeader(X_ACCEPT_VERSION_NAME, "3");

    // WHEN
    UnsupportedAcceptVersionException exception =
        assertThrows(
            UnsupportedAcceptVersionException.class,
            () ->
                acceptVersionInterceptor.preHandle(
                    httpServletRequest, httpServletResponse, "test"));

    // THEN
    assertThat(exception.getMessage())
        .isEqualTo(
            "Invalid major version passed in header X-Accept-Version (=3), accepted version is 2");
  }

  @Test
  void checkAcceptVersion_pass_when_application_version_valid() throws Exception {
    // GIVEN
    acceptVersionInterceptor =
        new AcceptVersionInterceptor(APPLICATION_ACCEPT_VERSION, X_ACCEPT_VERSION_NAME);
    httpServletRequest.addHeader(X_ACCEPT_VERSION_NAME, APPLICATION_ACCEPT_VERSION);

    // WHEN
    boolean accept =
        acceptVersionInterceptor.preHandle(httpServletRequest, httpServletResponse, "test");

    // THEN
    assertThat(accept).isTrue();
  }

  @Test
  void checkAcceptVersion_pass_when_header_accept_version_is_blank() throws Exception {
    // GIVEN
    acceptVersionInterceptor =
        new AcceptVersionInterceptor(APPLICATION_ACCEPT_VERSION, X_ACCEPT_VERSION_NAME);
    httpServletRequest.addHeader(X_ACCEPT_VERSION_NAME, "");

    // WHEN
    assertThrows(
        AcceptVersionMissingRequestHeaderException.class,
        () -> acceptVersionInterceptor.preHandle(httpServletRequest, httpServletResponse, "test"));
  }
}
