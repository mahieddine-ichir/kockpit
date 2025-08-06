package com.accor.wcp.sdk.application.config;

/**
 * SDK Configuration properties accessor.
 * Application, services and other components could access sdk-config properties.
 */
public interface SdkConfigurationProperties {

  InfrastructureType getInfrastructureType();

}
