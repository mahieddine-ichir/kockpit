package com.accor.wcp.audit.module.httpexchange;

import static org.mockserver.integration.ClientAndServer.startClientAndServer;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.model.StringBody.exact;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Header;

public class TestMockServer {

  private static ClientAndServer mockServer;

  @SuppressWarnings("resource")
  public static void createExpectationServer() {
    mockServer = startClientAndServer(8055);

    new MockServerClient("127.0.0.1", 8055)
        .when(
            request()
                .withMethod("POST")
                .withPath("/test")
                .withHeader("Content-type", "application/json")
                .withBody(exact("{username: 'foo', password: 'bar'}")))
        .respond(
            response()
                .withStatusCode(200)
                .withHeaders(
                    new Header("Content-Type", "application/json; charset=utf-8"),
                    new Header("Cache-Control", "public, max-age=86400"))
                .withBody("{ message: 'incorrect username and password combination' }")
                .withDelay(TimeUnit.SECONDS, 1));
  }

  public static void stop() {
    Optional.ofNullable(mockServer).ifPresent(MockServerClient::stop);
  }
}
