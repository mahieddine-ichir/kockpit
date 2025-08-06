package com.accor.wcp.web.rest.problem;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@SpringBootApplication
class SpringBoot4IntegrationTestApplication {

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(SpringBoot4IntegrationTestApplication.class);
        app.run(args);
    }
}

