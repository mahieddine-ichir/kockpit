package com.accor.kengine.seamless;

import static net.bytebuddy.matcher.ElementMatchers.isDeclaredBy;

import java.lang.reflect.InvocationTargetException;
import lombok.experimental.UtilityClass;
import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;

@UtilityClass
public class MultipleActionMethodsHelper {

  public static <T> T $(Class<T> aClass) {
    try {
      return new ByteBuddy()
          .subclass(aClass)
          .method(isDeclaredBy(aClass))
          .intercept(MethodDelegation.to(MultipleActionMethodReferenceRecorderInterceptor.class))
          .make()
          .load(aClass.getClassLoader(), ClassLoadingStrategy.Default.INJECTION)
          .getLoaded()
          .getDeclaredConstructor()
          .newInstance();
    } catch (InstantiationException
        | IllegalAccessException
        | InvocationTargetException
        | NoSuchMethodException e) {
      throw new RuntimeException(e);
    }
  }
}
