package org.kockpit.audit.module.web;

import lombok.experimental.UtilityClass;

/**
 * Will be refactored / review / (maybe) removed during next refactoring. This class has been
 * duplicated from wcp-rest-api (to remove the maven dependency).
 */
@UtilityClass
@Deprecated(since = "2.4.1", forRemoval = true)
class DefaultHeaderNames {

  static final String KOCKPIT_ORIGIN = "X-KOCKPIT-Origin";
}
