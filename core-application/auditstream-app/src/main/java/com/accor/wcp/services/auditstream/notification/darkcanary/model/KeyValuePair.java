package com.accor.wcp.services.auditstream.notification.darkcanary.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class KeyValuePair {

    private String key;

    private String value;
}
