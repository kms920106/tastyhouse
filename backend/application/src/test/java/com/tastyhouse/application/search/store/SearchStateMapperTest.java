package com.tastyhouse.application.search.store;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.search.model.PopularKeyword;
import com.tastyhouse.domain.search.model.SearchKeywordLog;

import static org.assertj.core.api.Assertions.assertThat;

class SearchStateMapperTest {

    @Test
    @DisplayName("PopularKeyword → PopularKeywordState → PopularKeyword 왕복 시 모든 필드가 보존된다")
    void popularKeywordRoundTrip() {
        PopularKeyword original = PopularKeyword.reconstitute(91L, "치킨", 2, true, false);

        PopularKeyword restored = PopularKeywordStateMapper.toDomain(PopularKeywordStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("PopularKeyword의 newKeyword·visible boolean이 뒤바뀌지 않는다")
    void popularKeywordBooleansAreNotSwapped() {
        PopularKeyword original = PopularKeyword.reconstitute(92L, "피자", 5, false, true);

        PopularKeyword restored = PopularKeywordStateMapper.toDomain(PopularKeywordStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("SearchKeywordLog → SearchKeywordLogState → SearchKeywordLog 왕복 시 모든 필드가 보존된다")
    void searchKeywordLogRoundTrip() {
        SearchKeywordLog original = SearchKeywordLog.reconstitute(93L, "떡볶이", LocalDateTime.of(2026, 9, 1, 12, 30));

        SearchKeywordLog restored = SearchKeywordLogStateMapper.toDomain(SearchKeywordLogStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }
}
