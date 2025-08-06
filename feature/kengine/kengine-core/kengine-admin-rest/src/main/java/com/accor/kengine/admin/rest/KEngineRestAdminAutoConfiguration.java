package com.accor.kengine.admin.rest;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "kengine.admin.rest.enable", havingValue = "true")
@ComponentScan("com.accor.kengine.admin.rest")
public class KEngineRestAdminAutoConfiguration {}
