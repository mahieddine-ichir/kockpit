package com.accor.wcp.console.services.sqsdlq.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collection;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SqsDlqSettingsDto {

  private String env;

  private String domain;

  private String subDomain;

  private String name;

  private String label;

  private String route;

  private String dlq;

  private String dlqArn;

  private String url;

  private Integer ttl;

  private Boolean deleteWhenReplay;

  private Integer concurrentConsumers;

  private Collection<SqsDlqColumnDefinitionDto> resultColumns;

  private String dedupIdGenerationStrategy;
}
