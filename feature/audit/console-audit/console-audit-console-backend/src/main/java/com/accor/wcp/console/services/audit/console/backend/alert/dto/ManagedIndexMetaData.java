package com.accor.wcp.console.services.audit.console.backend.alert.dto;

import com.fasterxml.jackson.annotation.JsonRootName;
import java.util.List;
import lombok.Data;

@Data
@JsonRootName("managed_index_meta_data")
public class ManagedIndexMetaData {

  public static final String ISM_INDEX_PATTERN = ".opendistro-ism-managed-index-history*";
  public static final String FIELD_TIMESTAMP = "managed_index_meta_data.history_timestamp";
  public static final String FIELD_ACTION_FAILED = "managed_index_meta_data.action.failed";
  public static final String FIELD_POLICY_ID = "managed_index_meta_data.policy_id.raw";

  private String index;
  private String index_uuid;
  private String policy_id;
  private int policy_seq_no;
  private int policy_primary_term;
  private boolean rolled_over;
  private long index_creation_date;
  private State state;
  private Action action;
  private RetryInfo retry_info;
  private Step step;
  private Info info;
  private long history_timestamp;

  @Data
  public class Action {
    private String name;
    private long start_time;
    private int index;
    private boolean failed;
    private int consumed_retries;
    private Object last_retry_time;
  }

  @Data
  public class Step {
    private String name;
    private long start_time;
    private String step_status;
  }

  @Data
  public class Info {
    private String message;
    private List<String> shard_failures;
  }

  @Data
  public class State {
    private String name;
    private long start_time;
  }

  @Data
  public class RetryInfo {
    public boolean failed;
    public int consumed_retries;
  }
}
