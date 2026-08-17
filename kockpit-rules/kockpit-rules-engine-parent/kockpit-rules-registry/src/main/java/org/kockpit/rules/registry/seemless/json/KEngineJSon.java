package org.kockpit.rules.registry.seemless.json;

import lombok.Data;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Data
public class KEngineJSon {
  private List<FlowJson> flowJsons;

  public KEngineJSon(InputStream inputStream) throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    this.flowJsons = objectMapper.readValue(inputStream, new TypeReference<>() {});
  }
}
