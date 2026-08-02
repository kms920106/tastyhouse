package com.tastyhouse.domain.search.domain.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.search.model.SearchKeywordLog;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 순수 도메인 모델 단위 테스트. Spring/JPA 컨텍스트 없이 도메인 로직만 검증한다.
 */
class SearchKeywordLogTest {

    @Test
    @DisplayName("of로 생성하면 미영속 상태(식별자 없음)이고 현재 시각으로 searchedAt이 설정된다")
    void of_createsTransientSearchKeywordLog() {
        LocalDateTime before = LocalDateTime.now();

        SearchKeywordLog log = SearchKeywordLog.of("파스타");

        LocalDateTime after = LocalDateTime.now();
        assertThat(log.getId()).isNull();
        assertThat(log.getKeyword()).isEqualTo("파스타");
        assertThat(log.getSearchedAt()).isBetween(before, after);
    }

    @Test
    @DisplayName("reconstitute는 DB 상태로부터 식별자·검색일시를 포함해 재구성한다")
    void reconstitute_restoresPersistedState() {
        LocalDateTime searchedAt = LocalDateTime.of(2026, 1, 1, 12, 0);

        SearchKeywordLog log = SearchKeywordLog.reconstitute(1L, "라면", searchedAt);

        assertThat(log.getId()).isEqualTo(1L);
        assertThat(log.getKeyword()).isEqualTo("라면");
        assertThat(log.getSearchedAt()).isEqualTo(searchedAt);
    }
}
