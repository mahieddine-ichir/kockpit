package com.accor.wcp.audit.module.autoconfigure.aws;

import com.accor.wcp.audit.module.aws.kinesis.AuditKinesisServiceInterceptor;
import com.accor.wcp.audit.module.aws.sqs.AuditSqsServiceInterceptor;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AwsServicesConfiguration {

  private static final String PROPERTY_KINESIS_STREAM_NAMES =
      "wcp.sdk.audit.aws.kinesis.stream-names";

  @Bean
  public InitializingBean awsServiceInitializingContextBean(ApplicationContext context) {
    return () -> {
      AuditSqsServiceInterceptor.setApplicationContext(context);
      AuditKinesisServiceInterceptor.setApplicationContext(context);
    };
  }

  @Bean
  public InitializingBean awsServiceRetrieveKinesisStreamNames(ApplicationContext context) {
    return () ->
        AuditKinesisServiceInterceptor.setKinesisStreamNames(retrieveKinesisStreamNames(context));
  }

  private List<String> retrieveKinesisStreamNames(ApplicationContext context) {
    if (context.getEnvironment().containsProperty(PROPERTY_KINESIS_STREAM_NAMES)) {
      return context.getEnvironment().getProperty(PROPERTY_KINESIS_STREAM_NAMES, List.class);
    }
    return Collections.emptyList();
  }
}
