package com.accor.wcp.services.auditstream.notification.darkcanary.config;

import lombok.Data;

@Data
public class TimeInterval {

    /**
     * may be hour of the day: 0, 1, 2, 3, ..., 23
     * or a range 8-20
     */
    private String hour;

    /**
     * day of week: MON, TUE
     * or a range: MON-TUE
     */
    private String day;
}
