package org.kockpit.rules.registry.seemless;

import lombok.experimental.UtilityClass;

@UtilityClass
public class NamingHelper {

  public static String normalizeComponentName(String name) {
    return Character.toLowerCase(name.charAt(0)) + name.substring(1);
  }
}
