package com.kockpit.rules.registry;

import com.kockpit.rules.DetailHandler;
import com.kockpit.rules.DocumentationDetails;
import com.kockpit.rules.SimpleDetail;

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
