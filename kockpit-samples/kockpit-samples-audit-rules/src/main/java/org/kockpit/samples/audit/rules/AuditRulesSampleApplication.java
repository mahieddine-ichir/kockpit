package org.kockpit.samples.audit.rules;

import org.kockpit.rules.seemless.Flow;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.thepavel.icomponent.InterfaceComponentScan;

@SpringBootApplication
@InterfaceComponentScan(annotation = Flow.class)
public class AuditRulesSampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuditRulesSampleApplication.class, args);
    }
}
