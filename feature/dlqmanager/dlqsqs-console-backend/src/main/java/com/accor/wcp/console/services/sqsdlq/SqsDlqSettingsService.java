package com.accor.wcp.console.services.sqsdlq;

import com.accor.wcp.console.services.sqsdlq.model.PartitionKey;
import com.accor.wcp.console.services.sqsdlq.model.SqsDlqSettingsDto;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SqsDlqSettingsService {

  private final SqsDlqManagerServiceActivator serviceActivator;

  public SqsDlqSettingsDto getSqsDlqSettingsDto(PartitionKey partitionKey) {
    return getSqsDlqSettingsDto(partitionKey.getEnv(), partitionKey.getQueueName(), null);
  }

  public SqsDlqSettingsDto getSqsDlqSettingsDto(String env, String queueName, String applicationId)
      throws NotFoundException {

    SqsDlqServiceManifest sqsServiceManifest = serviceActivator.getSqsServiceManifest();
    Optional<SqsDlqSettingsDto> settingsOptional =
        sqsServiceManifest.getSqsDlqSettingsDtos().stream()
            .filter(s -> s.getName().equals(queueName) || s.getDlq().equals(queueName))
            .filter(s -> s.getEnv().equals(env))
            .findFirst();

    if (settingsOptional.isEmpty()) {
      throw new NotFoundException("Queue config not found : " + queueName + " - " + applicationId);
    } else {
      return settingsOptional.get();
    }
  }
}
