package com.accor.wcp.console.services.sqsdlq.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class ApplicationProperties {
  @Value("${application.dynamodb_table_name.sqs}")
  private String tableName;

  @Value("${application.dynamodb_table_name.sqs_v2}")
  private String tableNameV2;

  private final String sqsDlqAsyncLogTableName = "sqsdlq_async_log";
}
