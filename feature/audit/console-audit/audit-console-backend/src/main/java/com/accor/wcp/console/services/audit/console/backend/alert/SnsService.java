package com.accor.wcp.console.services.audit.console.backend.alert;

import com.accor.wcp.console.services.audit.console.backend.alert.dto.ManagedIndexMetaData;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;
import software.amazon.awssdk.services.sns.model.SnsException;

@Service
@Slf4j
@RequiredArgsConstructor
public class SnsService {

  @Value("${aws.sns.topic_alert_es.arn}")
  private String topicSnsArn;

  @Value("${aws.env}")
  private String env;

  private final SnsClient snsClient;

  public void sendAlertEs(List<ManagedIndexMetaData> managedIndexOnErrorInfo) {
    if (!CollectionUtils.isEmpty(managedIndexOnErrorInfo)) {

      final String messageBody = getMessageBody(managedIndexOnErrorInfo);

      PublishRequest request =
          PublishRequest.builder()
              .subject("Alert elasticsearch in env : " + env.toUpperCase())
              .message(messageBody)
              .topicArn(topicSnsArn)
              .build();

      try {
        PublishResponse publish = snsClient.publish(request);
        log.info(
            "Alert sent to sns topic {}, response status is {} ",
            topicSnsArn,
            publish.sdkHttpResponse().statusCode());
      } catch (SnsException e) {
        log.error(
            "Error sending elasticsearch alert to sns topic {}, error is : {} ",
            topicSnsArn,
            e.awsErrorDetails().errorMessage());
        log.error("Alert message body was : {}", messageBody);
      }
    }
  }

  private String getMessageBody(List<ManagedIndexMetaData> managedIndexOnErrorInfo) {

    String policyIdOnErrorsDetail =
        managedIndexOnErrorInfo.stream()
            .map(
                m ->
                    " - Policy Id : "
                        + m.getPolicy_id()
                        + " on index : "
                        + m.getIndex()
                        + ", detail : "
                        + m.getInfo().getMessage())
            .collect(Collectors.joining("\n"));

    return "Error(s) : \n " + policyIdOnErrorsDetail;
  }
}
