package org.kockpit.rules.registry.registryapp;

import org.kockpit.rules.registry.RuleNodeRegistry;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(RuleNodeRegistry.class)
public class RuleRegistryApplication1 {}
