package com.accor.kengine.registry.seamless.v32;

import static com.accor.kengine.registry.seamless.v32.OldCompatibilityRule.BR_OLD_EXAMPLE;

import com.accor.kengine.DefaultDocumentationDetails;
import com.accor.kengine.DocumentationDetails;
import com.accor.kengine.registry.model.FlowEntry;
import java.util.Arrays;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
@Deprecated
public class OldCompatibilityFlow implements com.accor.kengine.registry.model.Flow {

  static class MyFlowEntry implements FlowEntry {

    private String id;

    public MyFlowEntry(String id) {
      this.id = id;
    }

    @Override
    public String getEntryId() {
      return id;
    }
  }

  @Override
  public String getId() {
    return getClass().getSimpleName();
  }

  @Override
  public DocumentationDetails getDetails() {
    return new DefaultDocumentationDetails(getId(), "Old compatibility flow example");
  }

  @Override
  public List<FlowEntry> getEntries() {
    return Arrays.asList(new MyFlowEntry(BR_OLD_EXAMPLE));
  }
}
