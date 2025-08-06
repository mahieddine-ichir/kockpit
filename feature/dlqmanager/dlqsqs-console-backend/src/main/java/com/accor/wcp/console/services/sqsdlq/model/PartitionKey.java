package com.accor.wcp.console.services.sqsdlq.model;

import static org.apache.commons.lang3.StringUtils.isBlank;

import com.accor.wcp.console.services.sqsdlq.dynamo.BadInputException;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@AllArgsConstructor
public class PartitionKey {
  public static final String KEY_SEPARATOR = "_";

  private final String domain;
  private final String env;
  private final String queueName;

  public static PartitionKeyBuilder builder() {
    return new PartitionKeyBuilder();
  }

  @Slf4j
  public static class PartitionKeyBuilder {
    private String domain;
    private String env;
    private String queueName;

    public PartitionKeyBuilder domain(String domain) {
      this.domain = domain;
      return this;
    }

    public PartitionKeyBuilder env(String env) {
      this.env = env;
      return this;
    }

    public PartitionKeyBuilder queueName(String queueName) {
      this.queueName = queueName;
      return this;
    }

    public PartitionKey build() {
      checkNotBlank("domain", domain);
      checkNotBlank("env", env);
      checkNotBlank("queueName", queueName);
      return new PartitionKey(domain, env, queueName);
    }

    private void checkNotBlank(String name, String value) {
      if (isBlank(value)) {
        throw new BadInputException(String.format("'%s' must not be blank!", name));
      }
    }
  }

  @Override
  public String toString() {
    return domain + KEY_SEPARATOR + env + KEY_SEPARATOR + queueName;
  }
}
