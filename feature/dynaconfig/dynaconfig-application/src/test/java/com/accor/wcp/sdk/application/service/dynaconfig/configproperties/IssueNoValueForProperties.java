package com.accor.wcp.sdk.application.service.dynaconfig.configproperties;

import com.accor.wcp.sdk.application.service.dynaconfig.DynaConfigAttribute;
import com.accor.wcp.sdk.application.service.dynaconfig.DynaConfigEnabler;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties specific to the application.
 *
 * <p>Properties are configured in the {@code application.yml} file.
 */
@ConfigurationProperties(prefix = "application.issue-no-value")
@Getter
@Setter
@DynaConfigEnabler
public class IssueNoValueForProperties {

  @DynaConfigAttribute("application.purchase-ttl-days")
  private String purchaseTtlDays;
}
