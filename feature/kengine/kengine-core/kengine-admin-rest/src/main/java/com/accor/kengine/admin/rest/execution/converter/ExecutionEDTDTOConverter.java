package com.accor.kengine.admin.rest.execution.converter;

import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import com.accor.kengine.KengineLog;
import com.accor.kengine.admin.rest.execution.dto.ExecutionEDTDTO;
import com.accor.kengine.admin.rest.execution.dto.RuleDetailEDTDTO;
import com.accor.kengine.admin.rest.execution.dto.RuleEDTDTO;
import com.accor.kengine.audit.model.Execution;
import com.accor.kengine.audit.model.ResultStatus;
import com.accor.kengine.audit.model.RuleExecution;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class ExecutionEDTDTOConverter {

  public ExecutionEDTDTO convert(Execution execution) throws IOException {
    ExecutionEDTDTO executionEDTDTO = new ExecutionEDTDTO();
    executionEDTDTO.setDate(execution.getDate());
    executionEDTDTO.setTime(execution.getTime());
    executionEDTDTO.setError(execution.getResultStatus().name());
    executionEDTDTO.setErrorDetails(execution.getDetail());
    executionEDTDTO.setErrorMessage(execution.getMessage());
    executionEDTDTO.setExecutionUUID(execution.getExecutionUuid());

    List<RuleEDTDTO> executionRules =
        execution.getRules().stream()
            .sorted(comparingInt(RuleExecution::getPosition))
            .map(
                rule -> {
                  RuleEDTDTO ruleEDTDTO = new RuleEDTDTO();
                  ruleEDTDTO.setDate(rule.getDate());
                  ruleEDTDTO.setTime(rule.getTime());
                  ruleEDTDTO.setError(rule.getResultStatus().name());
                  ruleEDTDTO.setErrorDetails(rule.getDetail());
                  ruleEDTDTO.setErrorMessage(rule.getMessage());

                  ruleEDTDTO.setName(rule.getCode());
                  ruleEDTDTO.setDetail(rule.getName());

                  return ruleEDTDTO;
                })
            .filter(ruleEDT -> !"BRROOT".equals(ruleEDT.getName()))
            .collect(toList());
    executionEDTDTO.setExecutionRules(executionRules);
    executionEDTDTO.setLogs(
        execution.getExecutionLogs().stream()
            .map(
                executionLog ->
                    KengineLog.builder()
                        .action(executionLog.getAction())
                        .date(executionLog.getTs())
                        .log(executionLog.getLog())
                        .build())
            .sorted(Comparator.comparing(KengineLog::getDate))
            .collect(Collectors.toList()));

    Map<String, List<RuleDetailEDTDTO>> ruleDetails =
        execution.getRules().stream()
            .sorted(comparingInt(RuleExecution::getPosition))
            .filter(rule -> !"BRROOT".equals(rule.getName()))
            .map(
                ruleExecution -> {
                  List<RuleDetailEDTDTO> ruleDetailEDTDTOS =
                      ruleExecution.getActionPredicates().stream()
                          .map(
                              actionPredicate -> {
                                RuleDetailEDTDTO ruleDetailEDTDTO = new RuleDetailEDTDTO();
                                ruleDetailEDTDTO.setDate(actionPredicate.getDate());
                                ruleDetailEDTDTO.setTime(actionPredicate.getTime());
                                if (actionPredicate.getResultStatus() == ResultStatus.ERROR
                                    || actionPredicate.getResultStatus() == ResultStatus.WARNING) {
                                  ruleDetailEDTDTO.setError(
                                      actionPredicate.getResultStatus().name());
                                }
                                if (actionPredicate.getResultStatus()
                                    == ResultStatus.CONDITION_FALSE) {
                                  ruleDetailEDTDTO.setCondition(false);
                                }
                                if (actionPredicate.getResultStatus()
                                    == ResultStatus.CONDITION_TRUE) {
                                  ruleDetailEDTDTO.setCondition(true);
                                }
                                ruleDetailEDTDTO.setErrorDetails(actionPredicate.getDetail());
                                ruleDetailEDTDTO.setErrorMessage(actionPredicate.getMessage());

                                ruleDetailEDTDTO.setName(actionPredicate.getCode());
                                ruleDetailEDTDTO.setDetail(actionPredicate.getName());
                                ruleDetailEDTDTO.setActionPredicate(
                                    actionPredicate.getTypeAP().name());
                                return ruleDetailEDTDTO;
                              })
                          .collect(toList());
                  String name = ruleExecution.getName();
                  if (name == null) {
                    name = "NONAME-" + ruleExecution.getId();
                  }
                  return new AbstractMap.SimpleEntry<>(name, ruleDetailEDTDTOS);
                })
            .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

    //		Map<String,List<RuleDetailEDTDTO>> ruleDetails =
    // rules.stream().filter(rule->!"BRROOT".equals(rule.getName())).flatMap(rule -> {
    //			return rule.getActionPredicates().stream();
    //		}).collect(
    //                Collectors.groupingBy(actionPredicate->actionPredicate.getRuleEDT().getName(),
    //                        Collectors.mapping(actionPredicate->{
    //                        	RuleDetailEDTDTO ruleDetailEDTDTO=new RuleDetailEDTDTO();
    //                        	ruleDetailEDTDTO.setDate(actionPredicate.getDate());
    //                        	ruleDetailEDTDTO.setTime(actionPredicate.getTime());
    //                        	if(actionPredicate.getResultStatus()== ResultStatus.ERROR ||
    // actionPredicate.getResultStatus()==ResultStatus.WARNING) {
    //                        		ruleDetailEDTDTO.setError(actionPredicate.getResultStatus().name());
    //                        	}
    //                        	if(actionPredicate.getResultStatus()==ResultStatus.CONDITION_FALSE) {
    //                        		ruleDetailEDTDTO.setCondition(false);
    //                        	}
    //                        	if(actionPredicate.getResultStatus()==ResultStatus.CONDITION_TRUE) {
    //                        		ruleDetailEDTDTO.setCondition(true);
    //                        	}
    //                        	ruleDetailEDTDTO.setErrorDetails(actionPredicate.getDetail());
    //                        	ruleDetailEDTDTO.setErrorMessage(actionPredicate.getMessage());
    //
    //                        	ruleDetailEDTDTO.setName(actionPredicate.getCode());
    //                        	ruleDetailEDTDTO.setDetail(actionPredicate.getName());
    //
    //	ruleDetailEDTDTO.setActionPredicate(actionPredicate.getTypeAP().name());
    //                        	return ruleDetailEDTDTO;
    //                        }, toList())
    //                )
    //        );

    executionEDTDTO.setRules(ruleDetails);
    executionEDTDTO.setRegistryId(execution.getRegistryId());

    //		List<RuleDto> rulesDTO= registryRuleService.getRegistryRule(execution.getRegistryId());

    //		Map<String,RuleDto> maprulesDTO=rulesDTO.stream().collect(Collectors.toMap(RuleDto::getName,
    // Function.identity()));

    //		executionEDTDTO.setReferential(maprulesDTO);

    return executionEDTDTO;
  }
}
