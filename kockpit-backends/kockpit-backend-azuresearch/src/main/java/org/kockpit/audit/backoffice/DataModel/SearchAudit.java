package org.kockpit.audit.backoffice.DataModel;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SearchAudit {

  private String type;

  private List<String> events;
}
