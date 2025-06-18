package org.kockpit.audit.api;

/**
 * Used to override default obfuscate field value
 *
 * @see ObfuscateAuditService.DEFAULT_OBFUSCATE_VALUE
 */
@Deprecated(since = "3.1.0", forRemoval = true)
public interface ObfuscateAuditFieldService {
  String getObfuscateValue(String fieldName, String fieldValue);
}
