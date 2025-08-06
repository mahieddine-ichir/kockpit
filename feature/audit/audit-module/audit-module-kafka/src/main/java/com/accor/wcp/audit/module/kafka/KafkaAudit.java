package com.accor.wcp.audit.module.kafka;

import com.accor.wcp.audit.AbstractAuditEvent;
import java.util.Map;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
@SuperBuilder
public class KafkaAudit extends AbstractAuditEvent {
  public static final String TYPE = "builtin.kafka";

  private KafkaMessageSource source;
  private String key;
  private String topic;
  private String payload;
  private String keyClassname;
  private String payloadClassname;
  private Integer partition;
  private Long timestamp;
  private Map<String, String> headers;
  private int serializedKeySize;
  private int serializedValueSize;
  private Long offset;
}
