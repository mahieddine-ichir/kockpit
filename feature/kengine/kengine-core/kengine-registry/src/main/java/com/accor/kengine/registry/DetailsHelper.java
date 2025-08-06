package com.accor.kengine.registry;

import com.accor.kengine.DetailHandler;
import com.accor.kengine.DocumentationDetails;
import com.accor.kengine.SimpleDetail;

public class DetailsHelper {

  private DetailsHelper() {
    // No constructor (helper pattern)
  }

  static DetailsSpecificationImpl computeName(
      DocumentationDetails details, DetailHandler detailHandler) {
    if (detailHandler == null) {
      return new DetailsSpecificationImpl("" + details);
    }
    SimpleDetail simpleDetail = detailHandler.handle(details);
    return new DetailsSpecificationImpl(
        simpleDetail.getCode(), simpleDetail.getName(), simpleDetail.getDescription());
  }
}
