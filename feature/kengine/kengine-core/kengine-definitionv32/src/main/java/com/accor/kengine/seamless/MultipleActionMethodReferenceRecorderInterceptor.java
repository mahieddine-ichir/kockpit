package com.accor.kengine.seamless;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import lombok.experimental.UtilityClass;
import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.BindingPriority;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;

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
