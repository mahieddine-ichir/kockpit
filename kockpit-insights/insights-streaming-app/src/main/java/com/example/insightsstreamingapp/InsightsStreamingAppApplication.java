package com.example.insightsstreamingapp;

import com.example.insightsstreamingapp.config.OpenSearchAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication(scanBasePackages = {
        "com.example.insightsstreamingapp",
        "org.kockpit.audit.stream"
})
@Import(OpenSearchAutoConfiguration.class)
public class InsightsStreamingAppApplication {

    public static void main(String[] args) {
        SpringApplication.run(InsightsStreamingAppApplication.class, args);
    }


}
