package com.example.kockpitbackendapplicationopensearch;

import org.kockpit.audit.backend.DomainApi;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
        "com.example.kockpitbackendserviceopensearch",
},scanBasePackageClasses = DomainApi.class)
public class KockpitBackendApplicationOpensearchApplication {

    public static void main(String[] args) {
        SpringApplication.run(KockpitBackendApplicationOpensearchApplication.class, args);
    }

}
