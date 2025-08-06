package com.accor.wcp.flow;

import com.accor.kengine.registry.model.FlowEntry;

public class DefaultFlowEntry implements FlowEntry {
  private final String entryId;

  public DefaultFlowEntry(String entryId) {
    this.entryId = entryId;
  }

  @Override
  public String getEntryId() {
    return entryId;
  }
}
