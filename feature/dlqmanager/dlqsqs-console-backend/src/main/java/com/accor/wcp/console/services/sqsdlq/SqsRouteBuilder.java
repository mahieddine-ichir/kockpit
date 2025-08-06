package com.accor.wcp.console.services.sqsdlq;

import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;

import com.accor.wcp.console.services.sqsdlq.config.ApplicationProperties;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.builder.RouteBuilder;

@Slf4j
class SqsRouteBuilder extends RouteBuilder {

  private final DynamoDbProcessor dynamoDbProcessor;
  private final SqsDlqServiceManifest sqsServiceManifest;

  static final String SQS_MESSAGE_ATTRIBUTE_NAMES = "All";
  static final String SQS_ATTRIBUTE_NAMES = "SentTimestamp,MessageGroupId";
  static final String HEADER_QUEUE_NAME = "QUEUE_NAME";
  static final String HEADER_ENVIRONMENT = "ENVIRONMENT";
  static final String HEADER_DOMAIN = "DOMAIN";

  private static final String SQS_URI_FORMAT =
      "aws2-sqs://%s?attributeNames=%s&messageAttributeNames=%s&amazonSQSClient=#amazonSQS&delay=5000&autoCreateQueue=false&exceptionHandler=#sqsExceptionHandler&greedy=true&concurrentConsumers=%s";

  private static final String DYNAMODB_URI_FORMAT =
      "aws2-ddb://%s?amazonDDBClient=#dynamoDbClientForSqsDlq&operation=PutItem";
  private List<String> routeIds = emptyList();

  private final ApplicationProperties applicationProperties;

  SqsRouteBuilder(
      DynamoDbProcessor dynamoDbProcessor,
      SqsDlqServiceManifest sqsServiceManifest,
      ApplicationProperties applicationProperties) {
    this.dynamoDbProcessor = dynamoDbProcessor;
    this.sqsServiceManifest = sqsServiceManifest;
    this.applicationProperties = applicationProperties;
  }

  @Override
  public void configure() {
    String uriDynamoDb = String.format(DYNAMODB_URI_FORMAT, applicationProperties.getTableNameV2());
    routeIds =
        sqsServiceManifest.getSqsDlqSettingsDtos().stream()
            .map(
                sqsDlqSettings -> {
                  String uriSqs =
                      String.format(
                          SQS_URI_FORMAT,
                          sqsDlqSettings.getDlqArn(),
                          SQS_ATTRIBUTE_NAMES,
                          SQS_MESSAGE_ATTRIBUTE_NAMES,
                          sqsDlqSettings.getConcurrentConsumers());
                  String routeId =
                      "sqsdlq-" + sqsDlqSettings.getName() + "-" + sqsDlqSettings.getDlqArn();
                  from(uriSqs)
                      .routeId(routeId)
                      .routeDescription(
                          "SQSDLQ Management for SQS "
                              + sqsDlqSettings.getName()
                              + " - "
                              + sqsDlqSettings.getUrl())
                      .autoStartup(true)
                      .threads(
                          sqsDlqSettings.getConcurrentConsumers(),
                          sqsDlqSettings.getConcurrentConsumers())
                      .filter(e -> nonNull(e.getIn().getBody()))
                      .setHeader(HEADER_DOMAIN, constant(sqsDlqSettings.getDomain()))
                      .setHeader(HEADER_ENVIRONMENT, constant(sqsDlqSettings.getEnv()))
                      .setHeader(HEADER_QUEUE_NAME, constant(sqsDlqSettings.getDlq()))
                      .process(dynamoDbProcessor)
                      .choice()
                      .when(simple("${header.CamelAwsDdbItem} != null"))
                      .to(uriDynamoDb);
                  return routeId;
                })
            .toList();
  }

  void destroy() {
    routeIds.forEach(this::stopAndRemoveRoute);
    routeIds = emptyList();
  }

  private void stopAndRemoveRoute(String id) {
    log.info("Stopping and removing SQSDLQ route: {}", id);
    try {
      getCamelContext().getRouteController().stopRoute(id);
    } catch (Exception e) {
      log.error("Error stopping route: {}.", id, e);
    }
    try {
      boolean removed = getCamelContext().removeRoute(id);
      if (!removed) {
        log.error("Camel did not remove the route: {}", id);
      }
    } catch (Exception e) {
      log.error("Error removing route: {}.", id, e);
    }
  }
}
