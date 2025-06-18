package com.kockpit.samples;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("sayhello")
@RequiredArgsConstructor
public class Api {

    private final MyFlow myFlow;

    @GetMapping("{name}")
    String sayHello(@PathVariable String name) {
        return myFlow.execute(name);
    }
}
