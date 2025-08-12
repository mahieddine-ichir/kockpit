package com.accor.wcp.services.auditstream.notification.darkcanary.config;

import lombok.Data;

import java.util.List;

@Data
public class DarkCanaryHeader {

    private String key;

    private List<String> values;
}
