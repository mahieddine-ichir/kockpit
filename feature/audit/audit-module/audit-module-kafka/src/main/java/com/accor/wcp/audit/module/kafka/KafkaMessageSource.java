package com.accor.wcp.audit.module.kafka;

public enum KafkaMessageSource {
  PRODUCE,
  PRODUCED_ACK,
  CONSUME,
  /** Kafka stream type */
  FORWARD,
}
