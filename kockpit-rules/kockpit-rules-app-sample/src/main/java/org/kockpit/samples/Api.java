package org.kockpit.samples;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api")
@RequiredArgsConstructor
@Slf4j
public class Api {

    private final MyFlow myFlow;

    @GetMapping("{name}")
    String sayHello(
            @PathVariable String name,
            @RequestParam(required = false, defaultValue = "false") Boolean upper
    ) {
        String execute = myFlow.execute(name, upper);
        log.info("evaluated flow for inputs: {}, {} => {}", name, upper, execute);
        return execute;
    }
}
