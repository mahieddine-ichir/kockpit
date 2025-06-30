package org.kockpit.rules.registry.model;

import org.kockpit.rules.DocumentationDetails;

import java.util.List;

/** KEngine flow definition. */
public interface Flow {

  /** Flow ID to identify it */
  String getId();

  /** Generic details object. */
  DocumentationDetails getDetails();

  /** Entries which implement the flow. It usually references rules. */
  List<FlowEntry> getEntries();
}
