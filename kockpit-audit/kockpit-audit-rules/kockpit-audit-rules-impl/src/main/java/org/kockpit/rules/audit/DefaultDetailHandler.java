package org.kockpit.rules.audit;

import org.kockpit.rules.DetailHandler;
import org.kockpit.rules.DocumentationDetails;
import org.kockpit.rules.SimpleDetail;

public class DefaultDetailHandler implements DetailHandler {

  @Override
  public SimpleDetail handle(DocumentationDetails documentationDetails) {
    if (documentationDetails == null) {
      return new SimpleDetail(null, null, null);
    }
    return new SimpleDetail(
          documentationDetails.getCode(),
          documentationDetails.getCode(),
          documentationDetails.getDocumentation());
  }
}
