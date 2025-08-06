package com.accor.wcp.console.services.audit.console.backend;

import com.accor.wcp.console.services.audit.kengine.KEngineRegistryRepository;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.elasticsearch.ElasticsearchRestClientAutoConfiguration;
import org.springframework.context.annotation.Import;

@SpringBootApplication(exclude = ElasticsearchRestClientAutoConfiguration.class)
@Import(KEngineRegistryRepository.class)
public class ServiceAuditConsoleBackendItApplication {}
