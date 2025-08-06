package com.accor.wcp.sample.benchhttp;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;

/**
 * IRMA Api return a huge size json, for profiling HTTP Exchange audit.
 */
@RestController
@RequestMapping("/bench")
public class BenchApi {

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping(value = "irma", produces = MediaType.APPLICATION_JSON_VALUE)
    String irma() throws IOException {
        return new String(this.getClass().getResourceAsStream("/irma/irma-response1.json").readAllBytes());
    }

    @GetMapping(value = "noirma", produces = MediaType.APPLICATION_JSON_VALUE)
    String noirma() {
        return """
                {
                "requestId": "1234",
                []
                }
                """;
    }

    @GetMapping(value = "hotel", produces = MediaType.APPLICATION_JSON_VALUE)
    String hotels(@RequestParam(value = "irma", defaultValue = "false") boolean irma) {
        String thisUri = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
        if (irma) {
            return restTemplate.getForObject(thisUri + "/bench/irma", String.class);
        } else {
            return restTemplate.getForObject(thisUri + "/bench/noirma", String.class);
        }
    }
}
