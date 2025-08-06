package com.accor.wcp.sdk.application.service.dynaconfig;

import com.accor.wcp.sdk.application.communication.App2WCPConsoleCommunicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.format.DateTimeFormatters;
import org.springframework.boot.autoconfigure.web.format.WebConversionService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;

import static java.util.Objects.isNull;

@Configuration
class ApplicationDynaConfigServiceAutoConfiguration {
    @Bean
    DynaConfigPropertiesServiceImpl dynaConfigPropertiesService() {
        return new DynaConfigPropertiesServiceImpl();
    }

    @Bean
    DynaConfigSpringBeanProcessor dynaConfigSpringBeanProcessor(ApplicationContext applicationContext,
                                                                App2WCPConsoleCommunicationService app2WCPConsoleCommunicationService,
                                                                DynaConfigPropertiesServiceImpl dynaConfigPropertiesService) {
        return new DynaConfigSpringBeanProcessor(applicationContext,
                app2WCPConsoleCommunicationService,
                dynaConfigPropertiesService);
    }

  @Bean
  DynaConfigApplicationServiceIntegration dynaConfigApplicationServiceIntegration(
      DynaConfigSpringBeanProcessor dynaConfigSpringBeanProcessor,
      App2WCPConsoleCommunicationService app2WCPConsoleCommunicationService,
      DynaConfigPropertiesServiceImpl dynaConfigPropertiesService,
      @Autowired(required = false) ConversionService conversionService,
      @Autowired(required = false) DynamicPropertyUpdateHandler dynamicPropertyUpdateHandler) {

    if (isNull(conversionService)) {
      conversionService = new WebConversionService((new DateTimeFormatters()));
    }

    if (isNull(dynamicPropertyUpdateHandler)) {
        dynamicPropertyUpdateHandler = new DefaultDynamicPropertyUpdateHandler(dynaConfigSpringBeanProcessor, conversionService);
    }
    return new DynaConfigApplicationServiceIntegration(app2WCPConsoleCommunicationService,
            dynaConfigSpringBeanProcessor,
            dynamicPropertyUpdateHandler,
            dynaConfigPropertiesService);
    }
}
