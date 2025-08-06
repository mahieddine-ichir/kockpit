package com.accor.wcp.sdk.application.config;

/**
 * Specific AWS configuration
 */
public class SdkConfigurationAws implements SdkConfigurationProperties {

  private final String accountId;

  public SdkConfigurationAws(String accountId) {
    this.accountId = accountId;
  }

  @Override
  public InfrastructureType getInfrastructureType() {
    return InfrastructureType.AWS;
  }

  public String getAccountId() {
    return accountId;
  }
}
