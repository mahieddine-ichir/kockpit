package com.accor.wcp.console.services.darkcanary.model;

import lombok.Data;

import java.util.List;

@Data
public class DarkCanaryHeader {

    private String key;

    private List<String> values;
}
