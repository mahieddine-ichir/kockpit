package com.accor.kengine.registry.seamless.json;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;

class KEngineJSonTest {

  @Test
  void getFlowJsons() throws IOException {
    KEngineJSon kEngineJSon = new KEngineJSon(getClass().getResourceAsStream("/flows-test1.json"));
    List<FlowJson> flowJsons = kEngineJSon.getFlowJsons();
    assertNotNull(flowJsons);
  }
}
