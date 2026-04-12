package org.kockpit.sample.api.dynaconfig;

import org.kockpit.communication.Message;
import org.kockpit.core.sdk.OnMessageListener;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

import static org.kockpit.features.dynaconfig.service.DynaConfigServiceDefinition.DYNA_CONFIG;

@RestController
@RequestMapping("/dyna-config")
public class DynaConfigPocApi implements OnMessageListener {

    private final Map<String, Object> keys = new HashMap<>();

    @GetMapping("keys")
    Map<String, Object> keys() {
        return keys;
    }

    @Override
    public void onMessage(Message message) {
        if (message.getService().equals(DYNA_CONFIG)) {
            message.getKeyValues().forEach(keyValue ->
                    keys.put(keyValue.key(), keyValue.value()));
        }
    }
}
