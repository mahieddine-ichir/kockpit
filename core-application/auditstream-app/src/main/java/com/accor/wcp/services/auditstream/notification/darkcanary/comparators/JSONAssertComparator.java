package com.accor.wcp.services.auditstream.notification.darkcanary.comparators;

import com.accor.wcp.services.auditstream.notification.darkcanary.config.DarkCanaryConfiguration;
import com.accor.wcp.services.auditstream.notification.darkcanary.model.PropertyDifference;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.skyscreamer.jsonassert.JSONCompare;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.skyscreamer.jsonassert.JSONCompareResult;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JSONAssertComparator {

    private final JsonListReorderer jsonListReorderer;

    @SneakyThrows
    public List<PropertyDifference> compare(String json1, String json2, DarkCanaryConfiguration darkCanaryConfiguration) {
        // reorder lists
        JSONCompareMode compareMode = JSONCompareMode.LENIENT;
        if (Boolean.TRUE == darkCanaryConfiguration.getStrict()) {
            compareMode = JSONCompareMode.STRICT;
        }
        String ordered1 = jsonListReorderer.order(json1);
        String ordered2 = jsonListReorderer.order(json2);
        JSONCompareResult jsonCompareResult = JSONCompare.compareJSON(ordered1, ordered2, compareMode);

        List<PropertyDifference> differences = new ArrayList<>();
        jsonCompareResult.getFieldMissing().stream().map(fieldComparisonFailure -> {
            PropertyDifference propertyDifference = PropertyDifference.of(
                    fieldComparisonFailure.getExpected() != null ? fieldComparisonFailure.getExpected().toString() : ""
            );
            propertyDifference.setLeftValue(fieldComparisonFailure.getExpected());
            return propertyDifference;
        }).forEach(differences::add);

        jsonCompareResult.getFieldUnexpected().stream().map(fieldComparisonFailure -> {
            PropertyDifference propertyDifference = PropertyDifference.of(
                    fieldComparisonFailure.getActual() != null ? fieldComparisonFailure.getActual().toString() : ""
            );
            propertyDifference.setRightValue(fieldComparisonFailure.getActual());
            return propertyDifference;
        }).forEach(differences::add);

        jsonCompareResult.getFieldFailures().stream()
                .map(fieldComparisonFailure -> {
                    PropertyDifference propertyDifference = PropertyDifference.of(fieldComparisonFailure.getField());
                    propertyDifference.setLeftValue(fieldComparisonFailure.getExpected());
                    propertyDifference.setRightValue(fieldComparisonFailure.getActual());
                    return propertyDifference;
                }).forEach(differences::add);

        return differences;
    }
}
