package org.kockpit.backend.services.search.opensearch;

import org.junit.jupiter.api.Test;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.TermQueryBuilder;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.opensearch.index.query.QueryBuilders.multiMatchQuery;

class OpensearchRepositoryAppIdsTest {

    private final BoolQueryBuilder freeText = new BoolQueryBuilder().should(multiMatchQuery("offers", "*"));

    @Test
    void without_app_ids_the_query_is_left_untouched() {
        assertThat(OpensearchRepository.restrictToAppIds(freeText, null)).isSameAs(freeText);
        assertThat(OpensearchRepository.restrictToAppIds(freeText, List.of())).isSameAs(freeText);
        assertThat(OpensearchRepository.restrictToAppIds(freeText, Arrays.asList(" ", null))).isSameAs(freeText);
    }

    @Test
    void app_ids_filter_wraps_the_query_so_its_should_clauses_stay_required() {
        BoolQueryBuilder restricted = OpensearchRepository.restrictToAppIds(freeText, List.of("offers-api", "offers-business-api"));

        // the original query is kept whole as a must: adding the filter next to its should
        // clauses would have made them optional
        assertThat(restricted.must()).containsExactly(freeText);
        assertThat(freeText.filter()).isEmpty();

        assertThat(restricted.filter()).hasSize(1);
        BoolQueryBuilder appIds = (BoolQueryBuilder) restricted.filter().getFirst();
        assertThat(appIds.should())
                .extracting(q -> ((TermQueryBuilder) q).fieldName() + "=" + ((TermQueryBuilder) q).value())
                .containsExactly("appId=offers-api", "appId=offers-business-api");
    }
}
