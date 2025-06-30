package org.kockpit.audit.sampleapp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kockpit.rules.RuleNodeException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("api")
@RequiredArgsConstructor
@Slf4j
public class Api {

    private final ApiFlow apiFlow;

    //private final RestTemplate restTemplate;
    // private final KEngineRuleNodeExecutorFactory kEngineRuleNodeExecutorFactory;

    @GetMapping("{name}")
    Map<?,?> name(@PathVariable String name) throws RuleNodeException {
        return Map.of("name", apiFlow.perform(name));
        /*
        RuleNode<Map<String, Object>> ruleNode = new RuleNodeBuilder<Map<String, Object>>()
                .ok()
                    .predicate(s -> {
                        log.info("Checking {} against {} ", s, name);
                        return s.get("name").equals("john");
                    })
                    .ok()
                        .action(context -> {
                            log.info("OK Action for {}", name);
                            String uri = ServletUriComponentsBuilder.fromCurrentContextPath()
                                    .path("api/v2/{name}")
                                    .buildAndExpand(name)
                                    .toUriString();
                            log.info("Calling URI: {}", uri);
                            Map<String, Object> forObject = restTemplate.getForObject(uri, Map.class);
                            context.put("output", forObject);
                        })
                    .ko()
                    .action(context -> {
                        log.info("!!! KO Action for {}", name);
                        context.put("output", Map.of("name", name));
                    })
                    .done()
                .done()
                .createRuleNode();

        RuleNodeExecutor<Map<String, Object>> ruleNodeExecutor = kEngineRuleNodeExecutorFactory.createRuleNodeExecutor();
        Map<String, Object> context = new HashMap<>();
        context.put("name", name);
        RuleNodeExecution ruleNodeExecution = ruleNodeExecutor.execute(ruleNode, context);
        log.info("RuleNodeExecution: {}", ruleNodeExecution);
        return (Map<?, ?>) context.get("output");

         */
    }

    @GetMapping("/v2/{name}")
    Map<String, String> nameV2(@PathVariable String name) {
        return Map.of("name", name.toUpperCase());
    }
}
