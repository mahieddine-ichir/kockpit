package com.accor.kengine.audit;

import com.accor.kengine.DetailHandler;
import com.accor.kengine.DocumentationDetails;
import com.accor.kengine.SimpleDetail;

@Deprecated
public class DefaultDetailHandler implements DetailHandler {

  @Override
  public SimpleDetail handle(DocumentationDetails detail) {
    if (detail instanceof DocumentationDetails) {
      DocumentationDetails documentationDetails = (DocumentationDetails) detail;
      return new SimpleDetail(
          documentationDetails.getCode(),
          documentationDetails.getCode(),
          documentationDetails.getDocumentation());
    }
    return new SimpleDetail("" + detail, "" + detail);
  }
}
