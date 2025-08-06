package com.accor.wcp.console.services.darkcanary.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DarkCanaryIdConfiguration {

    private String key;

    private String type; // header, body -> todo enum ?
}
