package com.accor.kengine.admin.rest.rule;

import com.accor.kengine.registry.RuleNodeRegistry;
import com.accor.kengine.registry.model.Registry;
import com.accor.kengine.registry.model.specification.RuleSpecification;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${kengine.admin.rest.endpoint.base:}/rules")
@CrossOrigin
@ConditionalOnBean(RuleNodeRegistry.class)
public class AdminRuleAPI {

  @Autowired private RuleNodeRegistry<Object> ruleNodeRegistry;

  @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
  public List<? extends RuleSpecification> list() {
    return ruleNodeRegistry.getCurrentRegistry().getRuleSpecifications();
  }

  public List<? extends RuleSpecification> getRuleDtos(Long registryId) {
    Optional<? extends Registry> optionalRegistry = ruleNodeRegistry.getRegistry(registryId);
    return optionalRegistry
        .<List<? extends RuleSpecification>>map(Registry::getRuleSpecifications)
        .orElse(null);
  }
}
