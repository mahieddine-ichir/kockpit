package com.accor.wcp.console.services.darkcanary.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DarkCanaryWatch {

    private String left;

    private String right;

    private String operator; // equals, equals_ignore-case, contains, starts_with, length_equals
}
