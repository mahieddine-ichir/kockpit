package com.accor.kengine.registry.registryapp;

import com.accor.kengine.registry.RuleNodeRegistry;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(RuleNodeRegistry.class)
public class RuleRegistryApplication1 {}
