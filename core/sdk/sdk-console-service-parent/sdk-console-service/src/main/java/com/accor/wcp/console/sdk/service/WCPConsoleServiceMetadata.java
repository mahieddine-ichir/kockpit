package com.accor.wcp.console.sdk.service;

import java.util.List;

/**
 * Console service metadata returned by service load.
 */
public interface WCPConsoleServiceMetadata {

    List<WCPConsoleServiceMenu> getMenus();

    default WCPConsoleServiceConfig getConfig() {
        return new WCPConsoleServiceConfig() {};
    }

}
