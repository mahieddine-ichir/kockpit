package com.accor.wcp.console.services.audit.console.backend.search.dto;

import static com.accor.wcp.console.services.audit.console.backend.search.dto.Operation.BETWEEN;
import static com.accor.wcp.console.services.audit.console.backend.search.dto.Operation.EQ;
import static com.accor.wcp.console.services.audit.console.backend.search.dto.Operation.GT;
import static com.accor.wcp.console.services.audit.console.backend.search.dto.Operation.GTE;
import static com.accor.wcp.console.services.audit.console.backend.search.dto.Operation.IN;
import static com.accor.wcp.console.services.audit.console.backend.search.dto.Operation.LT;
import static com.accor.wcp.console.services.audit.console.backend.search.dto.Operation.LTE;
import static com.accor.wcp.console.services.audit.console.backend.search.dto.Operation.NOT_BETWEEN;
import static com.accor.wcp.console.services.audit.console.backend.search.dto.Operation.NOT_EQ;
import static com.accor.wcp.console.services.audit.console.backend.search.dto.Operation.NOT_IN;

import java.util.Arrays;
import java.util.List;
import lombok.experimental.UtilityClass;

@UtilityClass
public class Constants {

  final List<Operation> LIST_BASED_OPS = Arrays.asList(EQ, NOT_EQ);

  final List<Operation> TEXT_BASED_OPS = Arrays.asList(EQ, NOT_EQ, IN, NOT_IN);

  final List<Operation> NUMBER_BASED_OPS =
      Arrays.asList(GT, GTE, LT, LTE, EQ, NOT_EQ, BETWEEN, NOT_BETWEEN);
}
