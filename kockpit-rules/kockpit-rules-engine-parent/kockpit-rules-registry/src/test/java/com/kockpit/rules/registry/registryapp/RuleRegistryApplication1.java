package com.kockpit.rules.registry.registryapp;

import com.kockpit.rules.registry.RuleNodeRegistry;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(RuleNodeRegistry.class)
public class RuleRegistryApplication1 {}
