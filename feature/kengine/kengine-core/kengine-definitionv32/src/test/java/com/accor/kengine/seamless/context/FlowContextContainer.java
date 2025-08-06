package com.accor.kengine.seamless.context;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.ToString;
import org.apache.commons.beanutils.LazyDynaBean;

@ToString
public class FlowContextContainer extends LazyDynaBean {

  public Map<Class<?>, Object> getContextData() {
    return contextData;
  }

  private Map<Class<?>, Object> contextData;

  private Map<String, Object> classNameContextData;

  public <T> T getContext(Class<T> clazz) {
    autoInit();
    return (T) contextData.get(clazz);
  }

  private void autoInit() {
    if (contextData == null) {
      contextData = initContextData();
      classNameContextData = new ConcurrentHashMap<>();
    }
  }

  protected Map<Class<?>, Object> initContextData() {
    return new ConcurrentHashMap<>();
  }

  public <T> Optional<T> getOptionalContext(Class<T> clazz) {
    autoInit();
    T t = null;
    if (contextData.containsKey(clazz)) {
      t = (T) contextData.get(clazz);
    }
    return Optional.ofNullable(t);
  }

  public <T> void setContext(Class<T> clazz, T context) {
    autoInit();
    contextData.put(clazz, context);
    classNameContextData.put(clazz.getSimpleName().toLowerCase(), context);
  }

  @Override
  public Object get(String name) {
    return classNameContextData.get(name.toLowerCase());
  }
}
