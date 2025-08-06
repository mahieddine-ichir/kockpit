package com.accor.wcp.sample.sampleflow.flow.context;

import com.accor.wcp.audit.annotation.Audited;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Audited
public class OperationContext {
    String result;
/*
    String valueResult;
*/
    String inputOp;
    String inputA;
    String inputB;

}
