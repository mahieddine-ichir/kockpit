package com.accor.wcp.console.services.sqsdlq.builder;

import com.accor.wcp.console.sdk.appmanifest.AppManifest;
import com.accor.wcp.console.sdk.service.impl.WCPConsoleServiceMenuDefault;
import com.accor.wcp.console.services.sqsdlq.model.SqsDlqSettingsDto;

public class WCPConsoleServiceMenuBuilder {

  private AppManifest appManifest;
  private SqsDlqSettingsDto settings;

  public WCPConsoleServiceMenuBuilder withAppManifest(AppManifest appManifest) {
    this.appManifest = appManifest;
    return this;
  }

  public WCPConsoleServiceMenuBuilder withSettings(SqsDlqSettingsDto settings) {
    this.settings = settings;
    return this;
  }

  public WCPConsoleServiceMenuDefault build() {
    String name = this.settings.getName();
    String route =
        String.format(
            "/services/sqsdlq/%s/%s/%s/%s",
            this.appManifest.getDomain(),
            this.appManifest.getEnv(),
            this.appManifest.getApplicationId(),
            name);

    return WCPConsoleServiceMenuDefault.builder()
        .id(name)
        .label(this.settings.getLabel())
        .route(route)
        .env(this.appManifest.getEnv())
        .domain(this.appManifest.getDomain())
        .subDomain(this.appManifest.getSubDomain())
        .app(this.appManifest.getApplicationId())
        .build();
  }
}
