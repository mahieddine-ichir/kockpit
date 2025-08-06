package com.accor.wcp.flow;

import static java.util.Objects.isNull;

import com.accor.wcp.flow.errors.ErrorCode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.beanutils.LazyDynaBean;

public class FlowContextContainer extends LazyDynaBean {

  public Map<Class<?>, Object> getContextData() {
    return contextData;
  }

  private transient Map<Class<?>, Object> contextData;
  private transient Map<String, Object> classNameContextData;

  private List<ErrorCode> warnings;

  public <T> T getContext(Class<T> clazz) {
    autoInit();
    return (T) contextData.get(clazz);
  }

  public void addWarning(ErrorCode warning) {
    if (isNull(this.warnings)) {
      this.warnings = new ArrayList<>();
    }
    this.warnings.add(warning);
  }

  public List<ErrorCode> getWarnings() {
    return Optional.ofNullable(this.warnings).orElse(Collections.emptyList());
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
    if (isNull(context)) {
      removeContext(clazz);
    } else {
      autoInit();
      contextData.put(clazz, context);
      classNameContextData.put(clazz.getSimpleName().toLowerCase(), context);
    }
  }

  public <T> void removeContext(Class<T> clazz) {
    autoInit();
    contextData.remove(clazz);
    classNameContextData.remove(clazz.getSimpleName().toLowerCase());
  }

  @Override
  public Object get(String name) {
    return classNameContextData.get(name.toLowerCase());
  }
}
