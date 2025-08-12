package com.accor.wcp.services.auditstream.notification.darkcanary.comparators;

import com.accor.wcp.services.auditstream.notification.darkcanary.JsonDiffComparator;
import com.accor.wcp.services.auditstream.notification.darkcanary.config.DarkCanaryConfiguration;
import com.accor.wcp.services.auditstream.notification.darkcanary.model.PropertyDifference;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.nonNull;

@Component
@Primary
@RequiredArgsConstructor
@Slf4j
public class JSONPathComparator implements JsonDiffComparator<String> {

    private final JSONAssertComparator jsonAssertComparator;

    @SneakyThrows
    @Override
    public List<PropertyDifference> compare(String json1, String json2, DarkCanaryConfiguration darkCanaryConfiguration) {
        DocumentContext context1 = JsonPath.parse(json1);
        DocumentContext context2 = JsonPath.parse(json2);

        if (! CollectionUtils.isEmpty(darkCanaryConfiguration.getExcludes())) {
            // remove any defined exclude
            // from json1
            darkCanaryConfiguration.getExcludes().stream()
                    .filter(darkCanaryWatch -> nonNull(darkCanaryWatch.getLeft()))
                    .forEach(watch -> context1.delete(watch.getLeft()));

            // from json2
            darkCanaryConfiguration.getExcludes().stream()
                    .filter(darkCanaryWatch -> nonNull(darkCanaryWatch.getRight()))
                    .forEach(watch -> context2.delete(watch.getRight()));
        }

        if (! CollectionUtils.isEmpty(darkCanaryConfiguration.getIncludes())) {
            // compare only inclusions
            return darkCanaryConfiguration.getIncludes().stream()
                    .map(watch -> {
                        Object read1 = read(context1, watch.getLeft());
                        Object read2 = read(context2, watch.getRight());
                        if (read1 != null && read2 != null) {
                            return this.compare(watch.getLeft(), read1, read2, Optional.ofNullable(watch.getOperator()));
                        } else {
                            PropertyDifference propertyDifference = PropertyDifference.of(watch.getLeft());
                            propertyDifference.setLeftValue(read1);
                            propertyDifference.setRightValue(read2);
                            return propertyDifference;
                        }
                    })
                    .filter(propertyDifference -> nonNull(propertyDifference.getLeftValue()) || nonNull(propertyDifference.getRightValue()))
                    .toList();
        } else {
            return jsonAssertComparator.compare(context1.jsonString(), context2.jsonString(), darkCanaryConfiguration);
        }
    }

    private Object read(DocumentContext documentContext, String jsonPath) {
        try {
            return documentContext.read(jsonPath);
        } catch (Exception e) {
            log.warn("Invalid JSON path {}", jsonPath, e);
            return null;
        }
    }

    private PropertyDifference compare(String property, Object v1, Object v2, Optional<String> operatorOptional) {
        String operator = operatorOptional.orElse("none");
        PropertyDifference propertyDifference = PropertyDifference.of(property);
        if (v1 instanceof String s1 && v2 instanceof String s2) {
            // on strings
            if (operator.equalsIgnoreCase("equals_ignore-case") && !s1.equalsIgnoreCase(s2)) {
                propertyDifference.setLeftValue(v1);
                propertyDifference.setRightValue(v2);
            } else if (operator.equalsIgnoreCase("starts_with") && !s1.startsWith(s2)) {
                propertyDifference.setLeftValue(v1);
                propertyDifference.setRightValue(v2);
            } else if (operator.equalsIgnoreCase("contains") && !s1.contains(s2)) {
                propertyDifference.setLeftValue(v1);
                propertyDifference.setRightValue(v2);
            } else if (!v1.equals(v2)) {
                // default
                propertyDifference.setLeftValue(v1);
                propertyDifference.setRightValue(v2);
            }
        } else if (v1 instanceof Collection<?> c1 && v2 instanceof Collection<?> c2) {
            // on collections
            if (operator.equalsIgnoreCase("length_equals")) {
                if (c1.size() != c2.size()) {
                    propertyDifference.setLeftValue(c1);
                    propertyDifference.setRightValue(c2);
                }
            } else {
                // default to compare contents
                Collection<Object> differentLeft = new HashSet<>(c1);
                differentLeft.removeAll(c2);
                if (!CollectionUtils.isEmpty(differentLeft)) {
                    propertyDifference.setLeftValue(differentLeft);
                }

                Collection<Object> differentRight = new HashSet<>(c2);
                differentRight.removeAll(c1);
                if (!CollectionUtils.isEmpty(differentRight)) {
                    propertyDifference.setRightValue(differentRight);
                }
            }
        } else {
            if (! v1.equals(v2)) {
                propertyDifference.setLeftValue(v1);
                propertyDifference.setRightValue(v2);
            }
        }
        return propertyDifference;
    }
}
