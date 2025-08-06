package com.accor.wcp.console.services.core.servicemanager;

import com.accor.wcp.console.sdk.service.WCPConsoleServiceActivator;
import com.accor.wcp.console.sdk.service.WCPConsoleServiceMetadata;

import java.util.Collection;
import java.util.Map;

public interface ServiceManager {
    Map<String, WCPConsoleServiceMetadata> getServiceMetadataMap();

    WCPConsoleServiceActivator getConsoleServiceActivator(String serviceId);

    Collection<String> getAuthorizedGroupsForDomainAndEnv(String domain, String env);
}
