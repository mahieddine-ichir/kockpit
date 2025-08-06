package com.accor.wcp.console.services.darkcanary;

import com.accor.wcp.console.services.darkcanary.model.DarkCanaryConfiguration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DarkCanaryService {

    private final DarkCanaryManagerServiceActivator serviceActivator;

    List<DarkCanaryConfiguration> getConfigurations() {
        return serviceActivator.getDarkCanaryServiceManifest().getDarkCanaryConfigurations();
    }

}
