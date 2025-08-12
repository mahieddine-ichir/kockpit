package com.accor.wcp.services.auditstream.config.health;

import org.opensearch.action.admin.cluster.health.ClusterHealthRequest;
import org.opensearch.action.admin.cluster.health.ClusterHealthResponse;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.cluster.health.ClusterHealthStatus;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;
import java.util.HashMap;

// ** {@link HealthIndicator} for an OpenSearch cluster using a {@link RestHighLevelClient}. */
@Component
class OpensearchRestClientHealthIndicator extends AbstractHealthIndicator {

  private final RestHighLevelClient client;

  public OpensearchRestClientHealthIndicator(RestHighLevelClient client) {
    super("Elasticsearch health check failed");
    this.client = client;
  }

  @Override
  protected void doHealthCheck(Health.Builder builder) throws Exception {
    ClusterHealthRequest healthRequest = new ClusterHealthRequest();
    ClusterHealthResponse response =
        this.client.cluster().health(healthRequest, RequestOptions.DEFAULT);
    ClusterHealthStatus clusterHealthStatus = response.getStatus();
    if (clusterHealthStatus == ClusterHealthStatus.RED) {
      builder.down();
      builder.withDetail("clusterStatus", clusterHealthStatus);
      builder.withDetail("requestStatus", response.status());
    } else {
      builder.up();
      HashMap<String, Object> details = new HashMap<>();
      details.put("clusterName", response.getClusterName());
      details.put("numberOfNodes", response.getNumberOfNodes());
      builder.withDetails(details);
    }
  }
}
