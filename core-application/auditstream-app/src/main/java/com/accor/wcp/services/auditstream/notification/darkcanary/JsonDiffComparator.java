package com.accor.wcp.services.auditstream.notification.darkcanary;

import com.accor.wcp.services.auditstream.notification.darkcanary.config.DarkCanaryConfiguration;
import com.accor.wcp.services.auditstream.notification.darkcanary.model.PropertyDifference;

import java.util.List;

/**
 * Compute the difference between two JSON source inputs.
 * @param <DIFF>
 * @param <JSON>
 */
public interface JsonDiffComparator<String> {

    List<PropertyDifference> compare(String left, String right, DarkCanaryConfiguration darkCanaryConfiguration);
}
