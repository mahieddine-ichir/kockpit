package com.kockpit.rules.seemless;

import lombok.experimental.UtilityClass;
import net.bytebuddy.implementation.bind.annotation.*;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * Tricky usage of interceptor to record called method of a proxy. It's a solution to get in {@link
 * #lastExecutedMethod} a method pointer dynamically.
 */
@UtilityClass
public class MultipleActionMethodReferenceRecorderInterceptor {

  static Method lastExecutedMethod;

  @RuntimeType
  @BindingPriority(0)
  public static Object interceptAndRecordCalledMethodSignature(
      @AllArguments Object[] args, @Origin Method method, @SuperCall Callable<?> callable) {
    lastExecutedMethod = method;
    return null;
  }
}
