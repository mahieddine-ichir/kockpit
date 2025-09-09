package com.accor.wcp.web.rest.config;

import java.io.CharArrayWriter;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.valves.AbstractAccessLogValve;
import org.apache.catalina.valves.RemoteIpValve;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;

@Slf4j
public class AccessLogConfig implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {
  private final ServerProperties serverProperties;

  public AccessLogConfig(ServerProperties serverProperties) {
    this.serverProperties = serverProperties;
  }

  @Override
  public void customize(TomcatServletWebServerFactory factory) {
    AbstractAccessLogValve accessLogValve =
        new AbstractAccessLogValve() {
          @Override
          protected void log(CharArrayWriter message) {
            log.debug(message.toString());
            MDC.clear();
          }
        };
    accessLogValve.setEnabled(serverProperties.getTomcat().getAccesslog().isEnabled());
    accessLogValve.setPattern(serverProperties.getTomcat().getAccesslog().getPattern());
    accessLogValve.setRequestAttributesEnabled(true);
    factory.addContextValves(new RemoteIpValve(), accessLogValve);
  }
}
