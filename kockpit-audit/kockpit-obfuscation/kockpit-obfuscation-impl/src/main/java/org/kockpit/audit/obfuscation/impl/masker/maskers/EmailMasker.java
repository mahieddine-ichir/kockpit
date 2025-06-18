package org.kockpit.audit.obfuscation.impl.masker.maskers;

import org.kockpit.audit.obfuscation.masker.Masker;

import static java.util.Objects.isNull;

public class EmailMasker implements Masker {

  private static final String REGEX_FOR_ONE_AT = "(?<=.)[^@](?=[^@]*?@)|(?:(?<=@.)|(?!^)\\G(?=[^@]*$)).";
  private static final String REGEX_FOR_MULTIPLE_AT = "[^@](?=[^@]*?@)|(?:(?<=@)|(?!^)(?=[^@]*$)).";

  @Override
  public String getType() {
    return "email";
  }

  @Override
  public String mask(String input) {
    if (isNull(input)) {
      return null;
    }
    long count = input.chars().filter(ch -> ch == '@').count();
    if (count < 1) {
      return input.replaceAll("(.)", "*");
    } else if (count == 1) {
      return input.replaceAll(REGEX_FOR_ONE_AT, "*");
    } else {
      return input.replaceAll(REGEX_FOR_MULTIPLE_AT, "*");
    }
  }
}
