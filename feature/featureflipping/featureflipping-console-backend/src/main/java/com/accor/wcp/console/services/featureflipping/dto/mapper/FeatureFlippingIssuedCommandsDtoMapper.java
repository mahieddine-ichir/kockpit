package com.accor.wcp.console.services.featureflipping.dto.mapper;

import com.accor.wcp.console.services.featureflipping.dto.IssuedCommandDto;
import com.accor.wcp.console.services.featureflipping.dto.RequestIssuedCommandDto;
import com.accor.wcp.console.services.featureflipping.dto.ResponseIssuedCommandDto;
import com.accor.wcp.console.services.featureflipping.instance.communication.dto.FeatureFlippingRequest;
import com.accor.wcp.console.services.featureflipping.instance.communication.dto.FeatureFlippingResponse;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.util.CollectionUtils;

@Mapper(componentModel = "spring")
public interface FeatureFlippingIssuedCommandsDtoMapper {

  default List<IssuedCommandDto> mapAllCommands(List<FeatureFlippingRequest> requests) {
    if (CollectionUtils.isEmpty(requests)) {
      return Collections.emptyList();
    }

    List<IssuedCommandDto> commands = new ArrayList<>();
    requests.forEach(request -> commands.addAll(mapRequest(request)));
    commands.addAll(mapResponses(requests));

    return commands.stream()
        .sorted(Comparator.comparingLong(IssuedCommandDto::getTimestamp).reversed())
        .toList();
  }

  default List<RequestIssuedCommandDto> mapRequest(FeatureFlippingRequest request) {
    if (CollectionUtils.isEmpty(request.getMessages())) {
      return Collections.emptyList();
    }

    return request.getMessages().stream()
        .map(
            requestDto ->
                mapRequestIssued(
                    request.getTimestamp(),
                    request.getRequestId(),
                    request.getInstanceId(),
                    requestDto.getPropertyName(),
                    requestDto.getNewValue()))
        .toList();
  }

  @Mapping(target = "type", constant = "REQUEST")
  @Mapping(target = "status", constant = "SENT")
  RequestIssuedCommandDto mapRequestIssued(Long timestamp, String requestId, String applicationInstance, String propertyName, String propertyValue);

  @Mapping(target = "type", constant = "RESPONSE")
  @Mapping(target = "applicationInstance", source = "instanceId")
  @Mapping(target = "status", source = "message.result")
  @Mapping(target = "message", source = "message.errorMessage")
  @Mapping(target = "propertyName", source = "message.propertyName")
  ResponseIssuedCommandDto mapResponse(FeatureFlippingResponse response);

  default List<ResponseIssuedCommandDto> mapResponses(List<FeatureFlippingRequest> requests) {
    return requests.stream()
        .map(FeatureFlippingRequest::getResponses)
        .flatMap(Collection::stream)
        .map(this::mapResponse)
        .toList();
  }
}
