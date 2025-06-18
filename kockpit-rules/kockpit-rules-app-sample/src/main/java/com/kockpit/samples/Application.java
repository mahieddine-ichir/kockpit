package com.kockpit.samples;

import com.kockpit.rules.seemless.Flow;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.thepavel.icomponent.InterfaceComponentScan;

@SpringBootApplication
@InterfaceComponentScan(annotation = Flow.class)
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
