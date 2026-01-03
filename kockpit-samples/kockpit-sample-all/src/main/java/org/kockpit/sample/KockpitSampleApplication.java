package org.kockpit.sample;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@Slf4j
public class KockpitSampleApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(KockpitSampleApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.getenv().forEach((k, v) -> log.trace("Env {}: {}", k, v));
    }

    @Bean
    RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
