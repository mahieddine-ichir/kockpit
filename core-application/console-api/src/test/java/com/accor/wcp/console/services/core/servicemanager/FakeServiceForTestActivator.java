package com.accor.wcp.console.services.core.servicemanager;

import static org.mockito.Mockito.mock;

import com.accor.wcp.console.sdk.appmanifest.AppManifest;
import com.accor.wcp.console.sdk.service.WCPConsoleServiceActivator;
import com.accor.wcp.console.sdk.service.WCPConsoleServiceMetadata;
import java.util.Collection;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("testmanifest")
public class FakeServiceForTestActivator implements WCPConsoleServiceActivator {
  @Override
  public String getServiceId() {
    return "fakefortest";
  }

  @Override
  public WCPConsoleServiceMetadata load(Collection<? extends AppManifest> appManifests) {
    System.out.println(appManifests);
    return mock(WCPConsoleServiceMetadata.class);
  }
}
