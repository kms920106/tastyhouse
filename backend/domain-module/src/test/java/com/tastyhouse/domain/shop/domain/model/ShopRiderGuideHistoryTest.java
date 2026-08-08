package com.tastyhouse.domain.shop.domain.model;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.shop.model.RiderGuideActionType;
import com.tastyhouse.domain.shop.model.RiderGuideActorType;
import com.tastyhouse.domain.shop.model.ShopRiderGuideHistory;
import com.tastyhouse.domain.shop.vo.ShopId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * append-only 이력 도메인 모델 단위 테스트.
 */
class ShopRiderGuideHistoryTest {

    @Test
    @DisplayName("of로 생성하면 미영속 상태이고 점주 변경 이력을 담는다")
    void of_createsTransientHistory() {
        ShopRiderGuideHistory history = ShopRiderGuideHistory.of(
            ShopId.of(1L), RiderGuideActorType.CEO, 7L, RiderGuideActionType.UPDATE,
            "이전 문구", "새 문구", null
        );

        assertThat(history.getId()).isNull();
        assertThat(history.getShopId()).isEqualTo(ShopId.of(1L));
        assertThat(history.getActorType()).isEqualTo(RiderGuideActorType.CEO);
        assertThat(history.getActorId()).isEqualTo(7L);
        assertThat(history.getActionType()).isEqualTo(RiderGuideActionType.UPDATE);
        assertThat(history.getPreviousVisitGuide()).isEqualTo("이전 문구");
        assertThat(history.getNewVisitGuide()).isEqualTo("새 문구");
        assertThat(history.getReason()).isNull();
        assertThat(history.getCreatedAt()).isNull();
    }

    @Test
    @DisplayName("관리자 삭제 조치 이력은 변경 후 문구가 null이고 사유가 담긴다")
    void of_createsDeletionHistory() {
        ShopRiderGuideHistory history = ShopRiderGuideHistory.of(
            ShopId.of(1L), RiderGuideActorType.ADMIN, 3L, RiderGuideActionType.DELETION,
            "마스크 미착용시 출입 금지", null, "가게 방문과 관련 없는 문구입니다."
        );

        assertThat(history.getActorType()).isEqualTo(RiderGuideActorType.ADMIN);
        assertThat(history.getActionType()).isEqualTo(RiderGuideActionType.DELETION);
        assertThat(history.getNewVisitGuide()).isNull();
        assertThat(history.getReason()).isEqualTo("가게 방문과 관련 없는 문구입니다.");
    }

    @Test
    @DisplayName("reconstitute는 DB 상태로부터 식별자·생성 시각을 포함해 재구성한다")
    void reconstitute_restoresPersistedState() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 8, 8, 21, 10);

        ShopRiderGuideHistory history = ShopRiderGuideHistory.reconstitute(
            12L, ShopId.of(5L), RiderGuideActorType.ADMIN, 3L, RiderGuideActionType.REVISION_REQUEST,
            "이전 문구", "이전 문구", "배차를 특정하는 문구입니다.", createdAt
        );

        assertThat(history.getId()).isEqualTo(12L);
        assertThat(history.getActionType()).isEqualTo(RiderGuideActionType.REVISION_REQUEST);
        assertThat(history.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    @DisplayName("RiderGuideActionType.from은 알 수 없는 코드에 대해 예외를 던진다")
    void actionTypeFrom_throwsException_whenUnknownCode() {
        assertThatThrownBy(() -> RiderGuideActionType.from("APPROVED"))
            .isInstanceOf(BusinessException.class)
            .extracting(exception -> ((BusinessException) exception).getErrorCode())
            .isEqualTo(ErrorCode.SHOP_RIDER_GUIDE_ACTION_TYPE_UNKNOWN);
    }

    @Test
    @DisplayName("RiderGuideActorType.from은 알 수 없는 코드에 대해 '변경 주체' 메시지로 예외를 던진다")
    void actorTypeFrom_throwsException_whenUnknownCode() {
        assertThatThrownBy(() -> RiderGuideActorType.from("RIDER"))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("변경 주체")
            .hasMessageContaining("RIDER")
            .extracting(exception -> ((BusinessException) exception).getErrorCode())
            .isEqualTo(ErrorCode.SHOP_RIDER_GUIDE_ACTION_TYPE_UNKNOWN);
    }
}
