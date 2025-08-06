package com.accor.wcp.console.services.sqsdlq;

import com.accor.wcp.console.services.sqsdlq.model.PartitionKey;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class InfiniteLoopRiskException extends RuntimeException {
  public InfiniteLoopRiskException(PartitionKey partitionKey) {
    super(
        String.format(
            """
                New DLQ messages have been detected since 'replay all' has been launched.
                It could lead to an infinite loop for domain: '%s', env: '%s', queueName: '%s'.
                If your application is not infinitely resending the same DLQ messages, relaunch 'replay all'.
                """,
            partitionKey.getDomain(), partitionKey.getEnv(), partitionKey.getQueueName()));
  }
}
