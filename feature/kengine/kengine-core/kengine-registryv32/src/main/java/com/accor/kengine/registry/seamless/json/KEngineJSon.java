package com.accor.kengine.registry.seamless.json;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import lombok.Data;

@Data
public class KEngineJSon {
  private List<FlowJson> flowJsons;

  public KEngineJSon(InputStream inputStream) throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    this.flowJsons = objectMapper.readValue(inputStream, new TypeReference<>() {});
  }
}
