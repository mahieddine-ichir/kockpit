package com.accor.wcp.services.auditstream.notification.service;

import java.io.File;
import org.junit.ClassRule;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
public class AuditReportServiceIt {

  @ClassRule
  public static DockerComposeContainer environment =
      new DockerComposeContainer(new File("src/test/resources/docker/docker-compose.yml"))
          .withExposedService(
              "elasticsearch_1",
              9092,
              Wait.forHttp("/all").forStatusCode(200).forStatusCode(401).usingTls());
}
