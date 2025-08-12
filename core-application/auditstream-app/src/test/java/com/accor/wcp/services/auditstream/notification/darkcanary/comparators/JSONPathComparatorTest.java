package com.accor.wcp.services.auditstream.notification.darkcanary.comparators;

import com.accor.wcp.services.auditstream.notification.darkcanary.config.ConfigurationProvider;
import com.accor.wcp.services.auditstream.notification.darkcanary.config.DarkCanaryConfiguration;
import com.accor.wcp.services.auditstream.notification.darkcanary.config.DarkCanaryWatch;
import com.accor.wcp.services.auditstream.notification.darkcanary.model.PropertyDifference;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SuppressWarnings("ALL")
@ExtendWith(SpringExtension.class)
@Import({JSONPathComparator.class, JsonListReorderer.class, JSONAssertComparator.class, ConfigurationProvider.class})
@Slf4j
class JSONPathComparatorTest {

    @Autowired
    private JSONPathComparator comparator;

    @MockitoBean
    private ConfigurationProvider configurationProvider;

    @BeforeEach
    void init() {
        when(configurationProvider.getConfiguration(anyString(), anyString())).thenReturn(Optional.of(testDarkCanaryConfiguration()));
    }

    @Test
    @DisplayName("Difference computation on differently ordered list, should pop no difference on list")
    void on_list_different_ordering() throws IOException {
        String json1 = new String(JSONPathComparatorTest.class.getResourceAsStream("/darkcanary_testing/offers_v1.json")
                        .readAllBytes());
        String json2 = new String(JSONPathComparatorTest.class.getResourceAsStream("/darkcanary_testing/offers_v2.json")
                        .readAllBytes());

        DarkCanaryConfiguration configuration = configurationProvider.getConfiguration("offers-api", "wcplatform")
                .stream()
                .filter(darkCanaryConfiguration -> darkCanaryConfiguration.getAppId().equals("offers-api"))
                .findFirst().get();

        List<PropertyDifference> differences = comparator.compare(json1, json2, configuration);
        // display diff in json format
        logDiff(differences);
        Assertions.assertEquals(3, differences.size());

        // request Hotel ID
        PropertyDifference requestHotelId = differences.stream()
                .filter(propertyDifference -> propertyDifference.getPropertyName().equalsIgnoreCase("$.request.hotelId"))
                .filter(propertyDifference -> Objects.nonNull(propertyDifference.getLeftValue()) && propertyDifference.getLeftValue().equals("1930"))
                .filter(propertyDifference -> Objects.nonNull(propertyDifference.getRightValue()) && propertyDifference.getRightValue().equals("1931"))
                .findAny()
                .or(Assertions::fail)
                .get();

        Assertions.assertNotEquals(requestHotelId.getLeftValue(), requestHotelId.getRightValue());
    }

    @Test
    @DisplayName("Difference computation on diff ordered list, no jsonPath config, should pop no difference")
    void on_list_different_ordering_with_nojson_path() throws IOException {
        String json1 = new String(JSONPathComparatorTest.class.getResourceAsStream("/darkcanary_testing/list-1.json")
                .readAllBytes());
        String json2 = new String(JSONPathComparatorTest.class.getResourceAsStream("/darkcanary_testing/list-2.json")
                .readAllBytes());

        DarkCanaryConfiguration configuration = DarkCanaryConfiguration.builder()
                .strict(true)
                .build();

        List<PropertyDifference> differences = comparator.compare(json1, json2, configuration);
        // display diff in json format
        logDiff(differences);
        Assertions.assertTrue(differences.isEmpty());
    }

    @Test
    @DisplayName("Difference computation on default config (none)")
    void with_no_config() throws IOException {
        String json1 = new String(JSONPathComparatorTest.class.getResourceAsStream("/darkcanary_testing/offers_v1.json")
                .readAllBytes());
        String json2 = new String(JSONPathComparatorTest.class.getResourceAsStream("/darkcanary_testing/offers_v2.json")
                .readAllBytes());

        List<PropertyDifference> differences = comparator.compare(json1, json2, DarkCanaryConfiguration.builder().build());
        // display diff in json format
        logDiff(differences);
        Assertions.assertEquals(3, differences.size());

        PropertyDifference requestHotelId = differences.stream()
                .filter(propertyDifference -> propertyDifference.getPropertyName().equalsIgnoreCase("request.hotelId"))
                .filter(propertyDifference -> Objects.nonNull(propertyDifference.getLeftValue()) && propertyDifference.getLeftValue().equals("1930"))
                .filter(propertyDifference -> Objects.nonNull(propertyDifference.getRightValue()) && propertyDifference.getRightValue().equals("1931"))
                .findAny()
                .or(Assertions::fail)
                .get();

        Assertions.assertNotEquals(requestHotelId.getLeftValue(), requestHotelId.getRightValue());
    }

    @Test
    @DisplayName("Comparing business offers EUR/USD")
    void diffComputationOnBestOffers() throws IOException {
        String jsonEur = new String(JSONPathComparatorTest.class.getResourceAsStream("/darkcanary_testing/best-offers-EUR.json")
                .readAllBytes());
        String jsonUsd = new String(JSONPathComparatorTest.class.getResourceAsStream("/darkcanary_testing/best-offers-USD.json")
                .readAllBytes());

        DarkCanaryConfiguration configuration = DarkCanaryConfiguration.builder()
                .strict(true)
                .build();

        List<PropertyDifference> differences = comparator.compare(jsonEur, jsonUsd, configuration);

        Assertions.assertFalse(differences.isEmpty());

        Assertions.assertEquals("bestOffers[0].offer.pricing.currency", differences.get(0).getPropertyName());
        Assertions.assertEquals("EUR", differences.get(0).getLeftValue());
        Assertions.assertEquals("USD", differences.get(0).getRightValue());

        Assertions.assertEquals("bestOffers[0].offer.pricing.main.amount", differences.get(1).getPropertyName());
        Assertions.assertEquals("411.5", differences.get(1).getLeftValue());
        Assertions.assertEquals("431", differences.get(1).getRightValue());

        Assertions.assertEquals("bestOffers[0].offer.pricing.main.formattedAmount", differences.get(2).getPropertyName());
        Assertions.assertEquals("€412", differences.get(2).getLeftValue());
        Assertions.assertEquals("$431", differences.get(2).getRightValue());
    }

    @Test
    @DisplayName("Comparing of completely different structued data")
    void diff_with_error() throws IOException {
        String jsonEuro = new String(JSONPathComparatorTest.class.getResourceAsStream("/darkcanary_testing/best-offers-EUR.json")
                .readAllBytes());
        String jsonError = new String(JSONPathComparatorTest.class.getResourceAsStream("/darkcanary_testing/error-400.json")
                .readAllBytes());

        DarkCanaryConfiguration configuration = DarkCanaryConfiguration.builder()
                .strict(true)
                .build();

        List<PropertyDifference> differences = comparator.compare(jsonEuro, jsonError, configuration);
        logDiff(differences);

        Assertions.assertFalse(differences.isEmpty());

        // on left only
        Assertions.assertEquals("bestOffers", differences.get(0).getPropertyName());
        Assertions.assertEquals("bestOffers", differences.get(0).getLeftValue());

        Assertions.assertEquals("errors", differences.get(1).getPropertyName());
        Assertions.assertEquals("errors", differences.get(1).getLeftValue());

        // on right only
        Assertions.assertTrue(differences.stream().anyMatch(
                propertyDifference -> propertyDifference.getPropertyName().equalsIgnoreCase("code")
                        && propertyDifference.getRightValue().equals("code")));

        Assertions.assertTrue(differences.stream().anyMatch(
                propertyDifference -> propertyDifference.getPropertyName().equalsIgnoreCase("detail")
                        && propertyDifference.getRightValue().equals("detail")));

        Assertions.assertTrue(differences.stream().anyMatch(
                propertyDifference -> propertyDifference.getPropertyName().equalsIgnoreCase("title")
                        && propertyDifference.getRightValue().equals("title")));
    }

    private void logDiff(List<PropertyDifference> differences) {
        differences.forEach(propertyDifference -> {
            try {
                String s = new ObjectMapper()
                        .enable(SerializationFeature.INDENT_OUTPUT)
                        .writeValueAsString(propertyDifference);
                log.info("\n{}\n", s);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
    }

    static DarkCanaryConfiguration testDarkCanaryConfiguration() {
        return DarkCanaryConfiguration.builder()
                .domain("wcplatform")
                .appId("offers-api")
                .includes(List.of(
                        DarkCanaryWatch.builder().left("$.request.hotelId").right("$.request.hotelId").build(),
                        DarkCanaryWatch.builder().left("$.offers[*]").right("$.offers[*]").build(),
                        DarkCanaryWatch.builder().left("$.offers[*].availability.status").right("$.offers[*].status").build(),
                        DarkCanaryWatch.builder().left("$.offers[?(@.type=ROOM)].hotel.id").right("$.offers[?(@.type=ROOM)].id").build()
                ))
                .build();
    }
}