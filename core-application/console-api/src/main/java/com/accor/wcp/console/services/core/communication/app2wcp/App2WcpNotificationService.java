package com.accor.wcp.console.services.core.communication.app2wcp;

import com.accor.wcp.console.sdk.service.WCPConsoleServiceActivator;
import com.accor.wcp.console.services.core.communication.app2wcp.model.ApplicationMessageNotificationDto;
import com.accor.wcp.console.services.core.servicemanager.ServiceManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static java.util.Objects.isNull;

@Slf4j
@Component
@RequiredArgsConstructor
class App2WcpNotificationService {
    private final ServiceManager serviceManager;

    void notify(ApplicationMessageNotificationDto applicationMessageNotificationDto) {
        String serviceId = applicationMessageNotificationDto.getServiceId();
        WCPConsoleServiceActivator consoleServiceActivator = serviceManager.getConsoleServiceActivator(serviceId);
        if (isNull(consoleServiceActivator)) {
            log.warn("No console service activator found for serviceId: {}", serviceId);
            return;
        }

        consoleServiceActivator.notification(applicationMessageNotificationDto);
    }
}
