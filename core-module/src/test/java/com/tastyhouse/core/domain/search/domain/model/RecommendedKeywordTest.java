package com.tastyhouse.core.domain.search.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 순수 도메인 모델 단위 테스트. 읽기 전용 애그리거트라 상태전이는 없고 reconstitute만 검증한다.
 */
class RecommendedKeywordTest {

    @Test
    @DisplayName("reconstitute는 DB 상태로부터 식별자를 포함해 재구성한다")
    void reconstitute_restoresPersistedState() {
        RecommendedKeyword recommendedKeyword = RecommendedKeyword.reconstitute(1L, "짜장면", 3, true);

        assertThat(recommendedKeyword.getId()).isEqualTo(1L);
        assertThat(recommendedKeyword.getKeyword()).isEqualTo("짜장면");
        assertThat(recommendedKeyword.getSortOrder()).isEqualTo(3);
        assertThat(recommendedKeyword.isVisible()).isTrue();
    }
}
