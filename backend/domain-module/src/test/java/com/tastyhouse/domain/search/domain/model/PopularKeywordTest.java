package com.tastyhouse.domain.search.domain.model;

import com.tastyhouse.domain.search.model.PopularKeyword;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 순수 도메인 모델 단위 테스트. Spring/JPA 컨텍스트 없이 도메인 로직만 검증한다.
 */
class PopularKeywordTest {

    @Test
    @DisplayName("of로 생성하면 미영속 상태(식별자 없음)이고 노출 상태다")
    void of_createsTransientPopularKeyword() {
        PopularKeyword popularKeyword = PopularKeyword.of("떡볶이", 1, true);

        assertThat(popularKeyword.getId()).isNull();
        assertThat(popularKeyword.getKeyword()).isEqualTo("떡볶이");
        assertThat(popularKeyword.getRank()).isEqualTo(1);
        assertThat(popularKeyword.isNewKeyword()).isTrue();
        assertThat(popularKeyword.isVisible()).isTrue();
    }

    @Test
    @DisplayName("reconstitute는 DB 상태로부터 식별자를 포함해 재구성한다")
    void reconstitute_restoresPersistedState() {
        PopularKeyword popularKeyword = PopularKeyword.reconstitute(1L, "김밥", 2, false, true);

        assertThat(popularKeyword.getId()).isEqualTo(1L);
        assertThat(popularKeyword.getKeyword()).isEqualTo("김밥");
        assertThat(popularKeyword.getRank()).isEqualTo(2);
        assertThat(popularKeyword.isNewKeyword()).isFalse();
        assertThat(popularKeyword.isVisible()).isTrue();
    }
}
