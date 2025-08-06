package com.accor.wcp.sdk.application.service.dynaconfig.configproperties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class ConfigurationPropertiesFieldsBrowserTest {

  @Test
  void readDynamicConfigOnConfigurationPropertiesClass() {
    ApplicationProperties applicationProperties = new ApplicationProperties();
    ApplicationProperties.Client client = new ApplicationProperties.Client();
    client.getAxa().setApikey("api-key1");
    applicationProperties.setClient(client);
    List<FoundField> foundFields =
        ConfigurationPropertiesFieldsBrowser.readDynaConfigAttributeOn(applicationProperties);
    assertNotNull(foundFields);
    assertEquals(8, foundFields.size());
    List<String> propertyNames =
        Arrays.asList(
            "application.client.wcxss-insurance-quotation.timeout",
            "application.client.axa.timeout",
            "application.client.axa.base-path",
            "application.client.axa.apikey",
            "application.client.axa.get-timeout-by-id",
            "application.client.apim.aps.timeout",
            "application.client.apim.order-api.timeout",
            "application.purchase-ttl-days");
    assertEquals(propertyNames, foundFields.stream().map(FoundField::getPropertyName).toList());
  }

  @Test
  void convertToLowerHyphens() {
    assertEquals(
        "wcxss-insurance-quotation",
        ConfigurationPropertiesFieldsBrowser.convertToLowerHyphens("wcxssInsuranceQuotation"));
    assertEquals(
        "base-path", ConfigurationPropertiesFieldsBrowser.convertToLowerHyphens("basePath"));
  }

  @Test
  void readDynamicConfigOnConfigurationPropertiesClassInfiniteLoop() {
    InfiniteLoopProperties applicationProperties = new InfiniteLoopProperties();
    InfiniteLoopProperties.Client client = applicationProperties.getClient();
    client.getAxa().setApikey("api-key1");
    List<FoundField> foundFields =
        ConfigurationPropertiesFieldsBrowser.readDynaConfigAttributeOn(applicationProperties);
    assertNotNull(foundFields);
    assertEquals(7, foundFields.size());
    List<String> propertyNames =
        Arrays.asList(
            "application.client.wcxss-insurance-quotation.timeout",
            "application.client.axa.timeout",
            "application.client.axa.base-path",
            "application.client.axa.apikey",
            "application.client.apim.aps.timeout",
            "application.client.apim.order-api.timeout",
            "application.purchase-ttl-days");
    assertEquals(propertyNames, foundFields.stream().map(FoundField::getPropertyName).toList());
  }

  @Test
  void should_convert_convertToLowerHyphens() {
    String s = ConfigurationPropertiesFieldsBrowser.convertToLowerHyphens("getTimeoutById");
    assertEquals("get-timeout-by-id", s);

    s = ConfigurationPropertiesFieldsBrowser.convertToLowerHyphens("mybigSentEnceWgetTimeoutById");
    assertEquals("mybig-sent-ence-wget-timeout-by-id", s);

    s = ConfigurationPropertiesFieldsBrowser.convertToLowerHyphens("anaisOauth2Authenticate");
    assertEquals("anais-oauth2-authenticate", s);

    s = ConfigurationPropertiesFieldsBrowser.convertToLowerHyphens("anaisOauth2Authenticate4");
    assertEquals("anais-oauth2-authenticate4", s);
  }

  @Test
  void readDynamicConfigOnConfigurationPropertiesClassWithNoDefaultValueAtInitialization() {
    IssueNoValueForProperties applicationProperties = new IssueNoValueForProperties();
    List<FoundField> foundFields =
            ConfigurationPropertiesFieldsBrowser.readDynaConfigAttributeOn(applicationProperties);
    assertNotNull(foundFields);
    assertEquals(1, foundFields.size());
    List<String> propertyNames =
            Arrays.asList(
                    "application.issue-no-value.purchase-ttl-days");
    assertEquals(propertyNames, foundFields.stream().map(FoundField::getPropertyName).toList());
  }
}
