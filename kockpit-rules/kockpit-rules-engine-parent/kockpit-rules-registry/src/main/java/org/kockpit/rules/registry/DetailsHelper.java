package org.kockpit.rules.registry;

import lombok.NoArgsConstructor;
import org.kockpit.rules.DetailHandler;
import org.kockpit.rules.DocumentationDetails;
import org.kockpit.rules.SimpleDetail;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class DetailsHelper {

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
