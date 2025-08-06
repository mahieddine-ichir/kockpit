package com.accor.wcp.sdk.application.service.featureflipping.provider.abtesting;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Map;

/**
 * Configuration example:
 * header.key:ff1
 * header.value:ENABLED
 */
@Slf4j
public class HeaderValueABTestingRule implements ABTestingRule {
    @Override
    public String id() {
        return "header";
    }

    @Override
    public boolean activate(Map<String, String> configs) {
        String headerKey = configs.get("header.key");
        String headerValue = configs.get("header.value");
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        // Only on Http context
        if (!(requestAttributes instanceof ServletRequestAttributes servletRequestAttributes)) {
            return false;
        }
        HttpServletRequest request = servletRequestAttributes.getRequest();
        String value = request.getHeader(headerKey);
        return headerValue.equalsIgnoreCase(value);
    }
}
