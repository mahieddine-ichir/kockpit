package com.accor.kengine.yaml;

import lombok.Getter;

public abstract class YamlFlowMapperCustomizer<T> {

  @Getter private final Class<T> typeParameterClass;

  public YamlFlowMapperCustomizer(Class<T> clazz) {
    this.typeParameterClass = clazz;
  }

  public abstract YamlFlow generateFlow(T element);
}
