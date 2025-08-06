package com.accor.wcp.sample.darkcanary;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequestMapping(value = "/darkcanary", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Slf4j
public class DarkCanaryTestingApi {

    private final CanaryService canaryService;

    @GetMapping(value="/offer")
    public Map<String, Object> gateway(@RequestHeader Map<String, String> headers, @RequestParam(name = "rate", defaultValue = "0.5") Double rate) {
        log.trace("Headers {}", headers);
        log.trace("Rate {}", rate);
        if (Math.random() < rate) {
            return canaryService.offers("v1");
        } else {
            return canaryService.offers("v2");
        }

    }

    @GetMapping("/offer/v1")
    Map<String, Object> offers() {
        return canaryService.offers("v1");
    }

    @GetMapping("/offer/v2")
    Map<String, Object> offersV2() {
        return canaryService.offers("v2");
    }
}
