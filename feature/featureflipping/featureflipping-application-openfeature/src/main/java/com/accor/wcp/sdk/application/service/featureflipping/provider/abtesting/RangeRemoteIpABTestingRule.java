package com.accor.wcp.sdk.application.service.featureflipping.provider.abtesting;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

/**
 * Configuration example:
 * range.ip.start:127.0.0.1
 * range.ip.end:127.0.0.2
 *
 * range.ip.start:10.10.145.1
 * range.ip.end:10.10.145.254
 *
 */
@Slf4j
public class RangeRemoteIpABTestingRule implements ABTestingRule {
    @Override
    public String id() {
        return "range";
    }

    @Override
    public boolean activate(Map<String, String> configs) {
        String rangeStart = configs.get("range.ip.start");
        String rangeEnd = configs.get("range.ip.end");
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        // Only on Http context
        if (!(requestAttributes instanceof ServletRequestAttributes servletRequestAttributes)) {
            return false;
        }
        HttpServletRequest request = servletRequestAttributes.getRequest();
        String remoteAddr = request.getRemoteAddr();

        try {
            return checkIPv4IsInRangeByConvertingToInt(remoteAddr, rangeStart, rangeEnd);
        } catch (UnknownHostException e) {
            log.info("Error checking ipv4: {} address in range {} / {}", remoteAddr, rangeStart, rangeEnd, e);
            return false;
        }
    }


    long ipToLongInt (InetAddress ipAddress) {
        long resultIP = 0;
        byte[] ipAddressOctets = ipAddress.getAddress();

        for (byte octet : ipAddressOctets) {
            resultIP <<= 8;
            resultIP |= octet & 0xFF;
        }
        return resultIP;
    }

    boolean checkIPv4IsInRangeByConvertingToInt (String inputIP, String rangeStartIP, String rangeEndIP)
            throws UnknownHostException {
        long startIPAddress = ipToLongInt(InetAddress.getByName(rangeStartIP));
        long endIPAddress = ipToLongInt(InetAddress.getByName(rangeEndIP));
        long inputIPAddress = ipToLongInt(InetAddress.getByName(inputIP));

        return (inputIPAddress >= startIPAddress && inputIPAddress <= endIPAddress);
    }
}
