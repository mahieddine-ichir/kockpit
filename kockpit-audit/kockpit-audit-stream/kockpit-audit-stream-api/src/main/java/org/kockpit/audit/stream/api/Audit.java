package org.kockpit.audit.stream.api;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class Audit {

  private String type;

  private List<Map<String, Object>> events;
}
