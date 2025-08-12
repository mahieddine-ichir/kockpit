package com.accor.wcp.services.auditstream.notification.darkcanary.comparators;

import com.accor.wcp.services.auditstream.notification.darkcanary.config.DarkCanaryConfiguration;
import com.accor.wcp.services.auditstream.notification.darkcanary.model.PropertyDifference;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.util.List;

@ExtendWith(SpringExtension.class)
@Import({JSONAssertComparator.class, JsonListReorderer.class})
@Slf4j
class JSONAssertDiffComparatorTest {

    @Autowired
    private JSONAssertComparator comparator;

    @Test
    void onBestOffers_EURvsUSD() throws IOException {
        DarkCanaryConfiguration darkCanaryConfiguration = DarkCanaryConfiguration.builder()
                .strict(true)
                .build();
        List<PropertyDifference> diffs = this.comparator.compare(
                new String(this.getClass().getResourceAsStream("/darkcanary_testing/best-offers-EUR.json").readAllBytes()),
                new String(this.getClass().getResourceAsStream("/darkcanary_testing/best-offers-USD.json").readAllBytes()),
                darkCanaryConfiguration
        );
        Assertions.assertFalse(diffs.isEmpty());

        Assertions.assertEquals("bestOffers[0].offer.pricing.currency", diffs.get(0).getPropertyName());
        Assertions.assertEquals("bestOffers[0].offer.pricing.main.amount", diffs.get(1).getPropertyName());
        Assertions.assertEquals("bestOffers[0].offer.pricing.main.formattedAmount", diffs.get(2).getPropertyName());
    }

    @Test
    @DisplayName("Difference computation on differently ordered list, should pop no difference")
    void on_list_different_ordering() throws IOException {
        String json1 = new String(JSONAssertDiffComparatorTest.class.getResourceAsStream("/darkcanary_testing/offers_v1.json")
                        .readAllBytes());
        String json2 = new String(JSONAssertDiffComparatorTest.class.getResourceAsStream("/darkcanary_testing/offers_v2.json")
                        .readAllBytes());

        List<PropertyDifference> differences = comparator.compare(json1, json2, DarkCanaryConfiguration.builder().build());
        differences.forEach(propertyDifference -> log.info(propertyDifference.toString()));

        // request HotelID
        PropertyDifference requestHotelId = differences.stream().filter(propertyDifference -> propertyDifference.getPropertyName().equalsIgnoreCase("request.hotelId"))
                .findAny()
                .or(Assertions::fail)
                .get();

        Assertions.assertNotEquals(requestHotelId.getLeftValue(), requestHotelId.getRightValue());

        // request lengthOfStay.unit
        PropertyDifference requestLengthOfStayUnit = differences.stream().filter(propertyDifference -> propertyDifference.getPropertyName().equalsIgnoreCase("request.lengthOfStay.unit"))
                .findAny()
                .or(Assertions::fail)
                .get();

        Assertions.assertNotEquals(requestLengthOfStayUnit.getLeftValue(), requestLengthOfStayUnit.getRightValue());
    }
}