package com.accor.wcp.sdk.application.service;

/**
 * Service definition for application side implementation. Service could implement this interface to
 * interact from platform side to application inside.
 */
public interface ServiceApplicationIntegration {
  /**
   * Unique service ID.
   *
   * @return service ID (must be unique / static)
   */
  String getServiceId();

  /**
   * Method is called by SDK to treat a message/notification from platform side.
   *
   * @param notification service message notification to treat
   */
  void notification(ServiceMessageNotification notification);

  /**
   * Initialize life cycle method.
   *
   * @return state after initialize
   */
  default ServiceApplicationState initialize() {
    return ServiceApplicationState.INITIALIZING;
  }

  /** @return current service application state */
  default ServiceApplicationState getState() {
    return ServiceApplicationState.RUNNING;
  }

  /**
   * Stop service.
   *
   * @return state after stop
   */
  default ServiceApplicationState stop() {
    return ServiceApplicationState.STOPPED;
  }
}
