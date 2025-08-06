package com.accor.wcp.console.services.sqsdlq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchRepositoriesAutoConfiguration;
import org.springframework.core.env.Environment;

@SpringBootApplication(exclude = ElasticsearchRepositoriesAutoConfiguration.class)
@Slf4j
public class ConsoleServicesApplicationForTest {

  /**
   * Main method, used to run the application.
   *
   * @param args the command line arguments.
   */
  public static void main(String[] args) {
    SpringApplication app = new SpringApplication(ConsoleServicesApplicationForTest.class);
    Environment env = app.run(args).getEnvironment();
  }
}
