package com.accor.wcp.sdk.application.service.featureflipping.provider;

import com.accor.wcp.sdk.application.service.featureflipping.FeatureFlippingKeysService;
import com.accor.wcp.sdk.application.service.featureflipping.provider.abtesting.ABTestingRule;
import com.accor.wcp.sdk.application.service.featureflipping.provider.abtesting.HeaderValueABTestingRule;
import com.accor.wcp.sdk.application.service.featureflipping.provider.abtesting.RangeRemoteIpABTestingRule;
import com.accor.wcp.sdk.application.service.featureflipping.provider.abtesting.ScheduledABTestingRule;
import dev.openfeature.sdk.*;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@Setter
@RequiredArgsConstructor
public class FeatureFlippingProvider implements FeatureProvider {
    private final FeatureFlippingKeysService flippingPropertiesService;

    private Map<String, ABTestingRule> abTestingRuleMap = new HashMap<>();
    @PostConstruct
    void initialize() {
        // TODO - generify
        RangeRemoteIpABTestingRule rangeRemoteIpABTestingRule = new RangeRemoteIpABTestingRule();
        HeaderValueABTestingRule headerValueABTestingRule = new HeaderValueABTestingRule();
        ScheduledABTestingRule scheduledABTestingRule = new ScheduledABTestingRule();
        abTestingRuleMap.put(rangeRemoteIpABTestingRule.id(), rangeRemoteIpABTestingRule);
        abTestingRuleMap.put(headerValueABTestingRule.id(), headerValueABTestingRule);
        abTestingRuleMap.put(scheduledABTestingRule.id(), scheduledABTestingRule);
    }

    @Override
    public Metadata getMetadata() {
        return new FeatureFlippingMetadata();
    }

    @Override
    public ProviderEvaluation<Boolean> getBooleanEvaluation(String key, Boolean aBoolean, EvaluationContext evaluationContext) {
        ProviderEvaluation<Boolean> evaluation = new ProviderEvaluation<>();
        Boolean booleanValue = flippingPropertiesService.getBoolean(key).orElse(aBoolean);
        // Feature flipping disable
        if (Boolean.FALSE.equals(booleanValue)) {
            evaluation.setValue(false);
            return evaluation;
        }
        // Feature flipping enable => A/B testing evaluation
        boolean evaluationResult = evalFeatureFlippingActivation(flippingPropertiesService.getEvaluationConfig(key));
        evaluation.setValue(evaluationResult);
        return evaluation;
    }

    private boolean evalFeatureFlippingActivation(Optional<Map<String, String>> evaluationConfig) {
        // No rule => activation by default
        if (evaluationConfig.isEmpty()) {
            return true;
        }

        // Eval
        Map<String, String> properties = evaluationConfig.get();
        List<String> ruleIds = properties.keySet().stream()
                .map(k -> k.substring(0, k.indexOf('.')))
                .distinct()
                .toList();

        return ruleIds.stream()
                .allMatch(ruleId -> evaluateRuleWithConfig(ruleId, properties));
    }

    private boolean evaluateRuleWithConfig(String ruleId, Map<String, String> evaluationConfig) {
        if (!abTestingRuleMap.containsKey(ruleId)) {
            return false;
        }
        ABTestingRule abTestingRule = abTestingRuleMap.get(ruleId);
        return abTestingRule.activate(evaluationConfig);
    }

    @Override
    public ProviderEvaluation<String> getStringEvaluation(String s, String defaultString, EvaluationContext evaluationContext) {
        ProviderEvaluation<String> evaluation = new ProviderEvaluation<>();
        String stringValue = flippingPropertiesService.getString(s).orElse(defaultString);
        evaluation.setValue(stringValue);
        return evaluation;
    }

    @Override
    public ProviderEvaluation<Integer> getIntegerEvaluation(String s, Integer defaultInteger, EvaluationContext evaluationContext) {
        ProviderEvaluation<Integer> evaluation = new ProviderEvaluation<>();
        Integer integerValue = flippingPropertiesService.getInteger(s).orElse(defaultInteger);
        evaluation.setValue(integerValue);
        return evaluation;
    }

    @Override
    public ProviderEvaluation<Double> getDoubleEvaluation(String s, Double aDouble, EvaluationContext evaluationContext) {
        return null;
    }

    @Override
    public ProviderEvaluation<Value> getObjectEvaluation(String s, Value value, EvaluationContext evaluationContext) {
        return null;
    }

    @Override
    public void initialize(EvaluationContext evaluationContext) throws Exception {
        FeatureProvider.super.initialize(evaluationContext);
    }

    @Override
    public void shutdown() {
        FeatureProvider.super.shutdown();
    }
}

@Getter
class FeatureFlippingMetadata implements Metadata {
    private final String name = "FeatureFlipping";
}