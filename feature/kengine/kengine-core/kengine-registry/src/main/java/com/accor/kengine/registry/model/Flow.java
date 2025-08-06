package com.accor.kengine.registry.model;

import com.accor.kengine.DocumentationDetails;
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
