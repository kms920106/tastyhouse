package com.tastyhouse.core.domain.shop.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 순수 도메인 모델 단위 테스트. 읽기 전용 애그리거트라 상태전이는 없고 reconstitute만 검증한다.
 */
class ProhibitedWordTest {

    @Test
    @DisplayName("reconstitute는 DB 상태로부터 식별자를 포함해 재구성한다")
    void reconstitute_restoresPersistedState() {
        ProhibitedWord prohibitedWord = ProhibitedWord.reconstitute(1L, "전화주문 유도", "전화주문 유도");

        assertThat(prohibitedWord.getId()).isEqualTo(1L);
        assertThat(prohibitedWord.getWord()).isEqualTo("전화주문 유도");
        assertThat(prohibitedWord.getReason()).isEqualTo("전화주문 유도");
    }

    @Test
    @DisplayName("reason은 nullable이며 null로도 재구성할 수 있다")
    void reconstitute_allowsNullReason() {
        ProhibitedWord prohibitedWord = ProhibitedWord.reconstitute(2L, "욕설", null);

        assertThat(prohibitedWord.getId()).isEqualTo(2L);
        assertThat(prohibitedWord.getWord()).isEqualTo("욕설");
        assertThat(prohibitedWord.getReason()).isNull();
    }
}
