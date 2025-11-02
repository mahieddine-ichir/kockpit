package org.kockpit.audit.stream;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@RequiredArgsConstructor
@Slf4j
public class KinesisStreamApplication {

    public static void main(String[] args) {
        SpringApplication.run(KinesisStreamApplication.class, args);
    }

}
