package com.accor.wcp.sample.trap;

import com.accor.wcp.sample.dynaconfig.ApplicationProperties;
import java.io.IOException;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import software.amazon.awssdk.utils.StringUtils;

@AllArgsConstructor
public class CustomHeaderInterceptor implements ClientHttpRequestInterceptor {

  private final ApplicationProperties properties;

  @Override
  public ClientHttpResponse intercept(
      HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
    String xCustomHeader = properties.getClient().getApim().getAps().getXCustomHeader();
    if (StringUtils.isNotBlank(xCustomHeader)) {
      request.getHeaders().add("x-custom-header", xCustomHeader);
    }
    return execution.execute(request, body);
  }
}
