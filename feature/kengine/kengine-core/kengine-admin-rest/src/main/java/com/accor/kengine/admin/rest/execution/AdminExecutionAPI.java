package com.accor.kengine.admin.rest.execution;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import com.accor.kengine.admin.rest.execution.converter.ExecutionEDTDTOConverter;
import com.accor.kengine.admin.rest.execution.dto.ExecutionEDTDTO;
import com.accor.kengine.admin.rest.rule.AdminRuleAPI;
import com.accor.kengine.audit.dao.ExecutionAuditDao;
import com.accor.kengine.audit.model.Execution;
import com.accor.kengine.registry.model.specification.RuleSpecification;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${kengine.admin.rest.endpoint.base:}/executions")
@CrossOrigin
@ConditionalOnBean(ExecutionAuditDao.class)
public class AdminExecutionAPI {

  @Autowired private ExecutionAuditDao executionAuditDao;

  @Autowired private ExecutionEDTDTOConverter executionEDTDTOConverter;

  @Autowired private AdminRuleAPI adminRuleAPI;

  @GetMapping(value = "/{uuid}", produces = MediaType.APPLICATION_JSON_VALUE)
  public @ResponseBody ExecutionEDTDTO get(@PathVariable("uuid") String uuid) throws IOException {
    Execution execution = executionAuditDao.load(UUID.fromString(uuid));
    if (execution == null) {
      throw new RuntimeException("TODO 404 not found exception");
    }

    ExecutionEDTDTO executionEDTDTO = executionEDTDTOConverter.convert(execution);

    Long registryId = execution.getRegistryId();
    Map<String, RuleSpecification> ruleDtoMap =
        adminRuleAPI.getRuleDtos(registryId).stream()
            .collect(toMap(RuleSpecification::getName, identity()));
    executionEDTDTO.setReferential(ruleDtoMap);

    return executionEDTDTO;
  }
}
