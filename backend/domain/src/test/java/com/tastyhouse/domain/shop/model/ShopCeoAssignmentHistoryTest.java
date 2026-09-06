package com.tastyhouse.domain.shop.model;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.ceo.vo.CeoId;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 가게-점주 접근권한 이력 순수 도메인 모델 단위 테스트.
 */
class ShopCeoAssignmentHistoryTest {

    @Test
    @DisplayName("of는 식별자·생성시각 없이 신규 이력을 만든다")
    void of_createsNewHistoryWithoutIdAndCreatedAt() {
        ShopCeoAssignmentHistory history = ShopCeoAssignmentHistory.of(
            ShopId.of(12L),
            CeoId.of(7L),
            ShopCeoAssignmentActionType.GRANT,
            99L
        );

        assertThat(history.getId()).isNull();
        assertThat(history.getCreatedAt()).isNull();
        assertThat(history.getShopId()).isEqualTo(ShopId.of(12L));
        assertThat(history.getCeoId()).isEqualTo(CeoId.of(7L));
        assertThat(history.getActionType()).isEqualTo(ShopCeoAssignmentActionType.GRANT);
        assertThat(history.getActorAdminId()).isEqualTo(99L);
    }

    @Test
    @DisplayName("reconstitute는 저장된 식별자·생성시각까지 복원한다")
    void reconstitute_restoresPersistedState() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 8, 14, 9, 12, 41);

        ShopCeoAssignmentHistory history = ShopCeoAssignmentHistory.reconstitute(
            512L,
            ShopId.of(12L),
            CeoId.of(7L),
            ShopCeoAssignmentActionType.REVOKE,
            99L,
            createdAt
        );

        assertThat(history.getId()).isEqualTo(512L);
        assertThat(history.getShopId()).isEqualTo(ShopId.of(12L));
        assertThat(history.getCeoId()).isEqualTo(CeoId.of(7L));
        assertThat(history.getActionType()).isEqualTo(ShopCeoAssignmentActionType.REVOKE);
        assertThat(history.getActorAdminId()).isEqualTo(99L);
        assertThat(history.getCreatedAt()).isEqualTo(createdAt);
    }
}
