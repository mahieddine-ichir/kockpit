package com.accor.wcp.services.auditstream.notification.es.manager.opensearch;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
class GetOpendistroPolicyResponse {

  @JsonProperty("_primary_term")
  private String primaryTerm;

  @JsonProperty("_seq_no")
  private String seqNo;

  private Policy policy;

  @Getter
  @JsonIgnoreProperties(ignoreUnknown = true)
  public static class Policy {
    @JsonProperty("schema_version")
    private int schemaVersion;
  }
}
