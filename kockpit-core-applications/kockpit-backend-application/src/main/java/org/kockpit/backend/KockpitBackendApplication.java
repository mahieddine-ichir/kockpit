package org.kockpit.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(
        scanBasePackages = "org.kockpit"
)
public class KockpitBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(KockpitBackendApplication.class, args);
    }

}
