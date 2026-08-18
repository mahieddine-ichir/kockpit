package org.kockpit.communication;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Regression : le tri chronologique des Message se faisait par soustraction, via
 * {@code Math.toIntExact(o1.getCreationDate() - o2.getCreationDate())}. creationDate etant un
 * long en millisecondes epoch, l'ecart depasse la capacite d'un int des que deux messages sont
 * separes de plus de 24 jours, et l'ArithmeticException remontait jusqu'au scheduler de
 * polling. Les cas ci-dessous echouent tous avec l'ancien comparateur.
 */
class MessageOrderingTest {

    private static Message at(long creationDate) {
        return Message.builder().id(String.valueOf(creationDate)).creationDate(creationDate).build();
    }

    @Test
    @DisplayName("Trie du plus ancien au plus recent")
    void sorts_oldest_first() {
        List<Message> sorted = Stream.of(at(3_000L), at(1_000L), at(2_000L))
                .sorted(Message.BY_CREATION_DATE)
                .toList();

        assertThat(sorted).extracting(Message::getCreationDate)
                .containsExactly(1_000L, 2_000L, 3_000L);
    }

    @Test
    @DisplayName("Un ecart de plus de 24 jours en millisecondes ne deborde pas")
    void handles_spread_beyond_int_range() {
        long now = Instant.parse("2026-08-18T08:41:50Z").toEpochMilli();
        long staleByThirtyDays = now - 30L * 24 * 3600 * 1000;
        assertThat(now - staleByThirtyDays).isGreaterThan(Integer.MAX_VALUE);

        assertThatCode(() -> Stream.of(at(now), at(staleByThirtyDays))
                .sorted(Message.BY_CREATION_DATE)
                .toList())
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Un message sans date, ou date en secondes, ne fait pas deborder le tri")
    void handles_mixed_units_and_missing_dates() {
        long millis = Instant.parse("2026-08-18T08:41:50Z").toEpochMilli();
        long seconds = millis / 1000;

        List<Message> sorted = Stream.of(at(millis), at(0L), at(seconds))
                .sorted(Message.BY_CREATION_DATE)
                .toList();

        assertThat(sorted).extracting(Message::getCreationDate)
                .containsExactly(0L, seconds, millis);
    }
}
