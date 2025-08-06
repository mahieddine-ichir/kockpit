package com.accor.wcp.sample.dynaconfig;

import static java.util.Objects.requireNonNullElse;

import com.accor.wcp.sdk.application.service.dynaconfig.DynaConfigAttribute;
import com.accor.wcp.sdk.application.service.dynaconfig.DynaConfigEnabler;
import java.util.Collections;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

@Configuration(proxyBeanMethods = false)
@DynaConfigEnabler
@Slf4j
public class DynamicPropertyOnConfiguration {

    @DynaConfigAttribute(
            value = "sample.resttemplate.timeout",
            beanPropertyAccess = true)
    private long clientQuotationTimeout;

    @DynaConfigAttribute(value = "application.client.axa.timeout", beanPropertyAccess = true)
    private long clientAxaTimeout;

    @DynaConfigAttribute(value = "application.client.aps.timeout", beanPropertyAccess = true)
    private long clientApsTimeout;

    @DynaConfigAttribute(
            value = "application.client.order-api.timeout",
            beanPropertyAccess = true)
    private long clientOrderApiTimeout;

    private HttpComponentsClientHttpRequestFactory clientQuotationRequestFactory;
    private HttpComponentsClientHttpRequestFactory clientAxaRequestFactory;
    private HttpComponentsClientHttpRequestFactory clientApsRequestFactory;
    private HttpComponentsClientHttpRequestFactory clientOrderApiRequestFactory;

    @Bean("restTemplateWcXssInsuranceQuotation")
    @Primary
    public RestTemplate wcXssQuotationRestTemplate(
            RestTemplateBuilder restTemplateBuilder, ApplicationProperties properties) {
        ApplicationProperties.Client.WcXssInsuranceQuotationConfig insuranceQuotationConfig =
                properties.getClient().getWcxssInsuranceQuotation();
        setClientQuotationTimeout(insuranceQuotationConfig.getTimeout());

        return buildRestTemplate(restTemplateBuilder, clientQuotationRequestFactory);
    }

    @Bean("restTemplateAXA")
    public RestTemplate axaRestTemplate(
            RestTemplateBuilder restTemplateBuilder, ApplicationProperties properties) {
        ApplicationProperties.Client.AxaClient axaProperties = properties.getClient().getAxa();
        setClientAxaTimeout(axaProperties.getTimeout());

        return buildRestTemplate(restTemplateBuilder, clientAxaRequestFactory);
    }

    @Bean("restTemplateAPS")
    public RestTemplate apsRestTemplate(
            RestTemplateBuilder restTemplateBuilder, ApplicationProperties properties) {
        ApplicationProperties.Client.ApimClient.ApimApsClient apsProperties = properties.getClient().getApim().getAps();
        setClientApsTimeout(apsProperties.getTimeout());

        return buildRestTemplate(restTemplateBuilder, clientApsRequestFactory);
    }

    @Bean("restTemplateOrderApi")
    public RestTemplate orderApiRestTemplate(
            RestTemplateBuilder restTemplateBuilder, ApplicationProperties properties) {
        ApplicationProperties.Client.ApimClient.ApimOrderApi apimOrderApi = properties.getClient().getApim().getOrderApi();
        setClientOrderApiTimeout(apimOrderApi.getTimeout());

        return buildRestTemplate(restTemplateBuilder, clientOrderApiRequestFactory);
    }

    private RestTemplate buildRestTemplate(
            RestTemplateBuilder restTemplateBuilder, HttpComponentsClientHttpRequestFactory factory) {
        RestTemplate restTemplate =
                restTemplateBuilder.errorHandler(new DefaultResponseErrorHandler()).build();

        // Force BufferingRequestFactory & interceptors for RestTemplateInterceptorInjector
        restTemplate.setRequestFactory(new BufferingClientHttpRequestFactory(factory));
        restTemplate.setInterceptors(Collections.emptyList());
        return restTemplate;
    }

    public void setClientQuotationTimeout(long timeout) {
        clientQuotationRequestFactory =
                requireNonNullElse(
                        clientQuotationRequestFactory, new HttpComponentsClientHttpRequestFactory());
        this.clientQuotationTimeout = timeout;
        updateRequestFactoryTimeout(clientQuotationRequestFactory, timeout);
    }

    public void setClientAxaTimeout(long timeout) {
        clientAxaRequestFactory =
                requireNonNullElse(clientAxaRequestFactory, new HttpComponentsClientHttpRequestFactory());
        this.clientAxaTimeout = timeout;
        updateRequestFactoryTimeout(clientAxaRequestFactory, timeout);
    }

    public void setClientApsTimeout(long timeout) {
        clientApsRequestFactory =
                requireNonNullElse(clientApsRequestFactory, new HttpComponentsClientHttpRequestFactory());
        this.clientApsTimeout = timeout;
        updateRequestFactoryTimeout(clientApsRequestFactory, timeout);
    }

    public void setClientOrderApiTimeout(long timeout) {
        clientOrderApiRequestFactory =
                requireNonNullElse(
                        clientOrderApiRequestFactory, new HttpComponentsClientHttpRequestFactory());
        this.clientOrderApiTimeout = timeout;
        updateRequestFactoryTimeout(clientOrderApiRequestFactory, timeout);
    }

    private void updateRequestFactoryTimeout(
            HttpComponentsClientHttpRequestFactory factory, long timeout) {
        factory.setConnectTimeout((int) timeout);
        factory.setConnectionRequestTimeout((int) timeout);
    }

    @Bean
    InitializingBean testToLoadMetadataConfigurationPropertiesAnnotatedBeans(ApplicationContext applicationContext) {
        return () -> {
            Map<String, Object> configurationPropertiesBeans = applicationContext.getBeansWithAnnotation(ConfigurationProperties.class);
            log.info("configurationPropertiesBeans: {}", configurationPropertiesBeans);
        };
    }
}
