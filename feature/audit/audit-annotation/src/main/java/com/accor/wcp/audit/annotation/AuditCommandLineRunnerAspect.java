package com.accor.wcp.audit.annotation;

import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

@Aspect
@Slf4j
public class AuditCommandLineRunnerAspect {

  private final Environment environment;
  private final ConfigurableApplicationContext context;

  public AuditCommandLineRunnerAspect(
      Environment environment, ConfigurableApplicationContext context) {
    this.environment = environment;
    this.context = context;
  }

  @Before("@annotation(auditCommandLineRunner)")
  public void before(AuditCommandLineRunner auditCommandLineRunner) {
    logApplicationStartup(environment);
  }

  @After("@annotation(auditCommandLineRunner)")
  public void after(AuditCommandLineRunner auditCommandLineRunner) {
    logApplicationFinish(environment);
    forceStopApp();
  }

  protected static void logApplicationStartup(Environment env) {
    log.info(
        "\n----------------------------------------------------------\n\tApplication '{}' is running!\n\tProfile(s): \t{}\n----------------------------------------------------------",
        env.getProperty("spring.application.name"),
        env.getActiveProfiles());
  }

  protected static void logApplicationFinish(Environment env) {
    log.info(
        "\n----------------------------------------------------------\n\tApplication '{}' terminated!\n----------------------------------------------------------",
        env.getProperty("spring.application.name"));
  }

  protected void forceStopApp() {
    // Used since aws scheduled task is stuck in running state
    if (Objects.isNull(context)) {
      System.exit(0);
    } else {
      System.exit(SpringApplication.exit(context));
    }
  }
}
