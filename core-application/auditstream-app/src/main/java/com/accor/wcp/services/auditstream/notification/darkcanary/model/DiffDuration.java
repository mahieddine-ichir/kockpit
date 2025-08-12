package com.accor.wcp.services.auditstream.notification.darkcanary.model;

import lombok.Data;

@Data
public class DiffDuration {

    private Long leftDuration;

    private Long rightDuration;

    private Long difference;
}
