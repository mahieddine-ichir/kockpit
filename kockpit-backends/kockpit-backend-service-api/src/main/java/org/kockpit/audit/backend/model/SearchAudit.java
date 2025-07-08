package org.kockpit.audit.backend.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SearchAudit {

  private String type;

  private List<String> events;
}
