package com.accor.wcp.console.services;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchClientAutoConfiguration;
import org.springframework.core.env.Environment;

@SpringBootApplication(
    exclude = {
      ElasticsearchRepositoriesAutoConfiguration.class,
      ElasticsearchClientAutoConfiguration.class
    })
@Slf4j
public class ConsoleServicesApplication {

  /**
   * Main method, used to run the application.
   *
   * @param args the command line arguments.
   */
  public static void main(String[] args) {
    SpringApplication app = new SpringApplication(ConsoleServicesApplication.class);
    Environment env = app.run(args).getEnvironment();
    logApplicationStartup(env);
  }

  private static void logApplicationStartup(Environment env) {
    String protocol =
        Optional.ofNullable(env.getProperty("server.ssl.key-store"))
            .map(key -> "https")
            .orElse("http");
    String serverPort = env.getProperty("server.port");
    String contextPath =
        Optional.ofNullable(env.getProperty("server.servlet.context-path"))
            .filter(StringUtils::isNotBlank)
            .orElse("/");
    String hostAddress = "localhost";
    try {
      hostAddress = InetAddress.getLocalHost().getHostAddress();
    } catch (UnknownHostException e) {
      log.warn("The host name could not be determined, using `localhost` as fallback");
    }
    // ES specific
    String opensearchEndpoint = env.getProperty("spring.opensearch.rest.uris");

    log.info(
        "\n----------------------------------------------------------\n\t"
            + "Application '{}' is running! Access URLs:\n\t"
            + "Local: \t\t{}://localhost:{}{}\n\t"
            + "External: \t{}://{}:{}{}\n\t"
            + "Profile(s): {}\n\t"
            + "Opensearch:\n\t\t - endpoint: \t{}\n\t\t"
            + "----------------------------------------------------------",
        env.getProperty("spring.application.name"),
        protocol,
        serverPort,
        contextPath,
        protocol,
        hostAddress,
        serverPort,
        contextPath,
        env.getActiveProfiles(),
        opensearchEndpoint);
  }
}
