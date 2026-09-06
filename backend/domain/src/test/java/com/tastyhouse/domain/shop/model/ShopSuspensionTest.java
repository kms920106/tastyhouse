package com.tastyhouse.domain.shop.model;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import com.tastyhouse.domain.shared.model.OrderMethod;
import com.tastyhouse.domain.shop.vo.ShopId;

/**
 * 순수 도메인 모델 단위 테스트. Spring/JPA 컨텍스트 없이 도메인 로직만 검증한다
 * (도메인/JPA 엔티티 분리로 얻는 테스트 용이성의 레퍼런스).
 */
class ShopSuspensionTest {

    @Test
    @DisplayName("of로 생성하면 미영속 상태(식별자·감사 시각·해제 시각 없음)이고 사유·기간을 담는다")
    void of_createsTransientShopSuspension() {
        LocalDateTime startAt = LocalDateTime.of(2026, 8, 1, 10, 0);
        LocalDateTime endAt = LocalDateTime.of(2026, 8, 1, 12, 0);

        ShopSuspension shopSuspension = ShopSuspension.of(ShopId.of(1L), SuspensionReason.BAD_WEATHER, OrderMethod.DELIVERY, startAt, endAt);

        assertThat(shopSuspension.getId()).isNull();
        assertThat(shopSuspension.getShopId()).isEqualTo(ShopId.of(1L));
        assertThat(shopSuspension.getReason()).isEqualTo(SuspensionReason.BAD_WEATHER);
        assertThat(shopSuspension.getOrderMethod()).isEqualTo(OrderMethod.DELIVERY);
        assertThat(shopSuspension.getStartAt()).isEqualTo(startAt);
        assertThat(shopSuspension.getEndAt()).isEqualTo(endAt);
        assertThat(shopSuspension.getReleasedAt()).isNull();
        assertThat(shopSuspension.getCreatedAt()).isNull();
    }

    @Test
    @DisplayName("종료시간이 시작시간보다 이전이면 예외가 발생한다")
    void of_throwsException_whenEndAtBeforeStartAt() {
        LocalDateTime startAt = LocalDateTime.of(2026, 8, 1, 12, 0);
        LocalDateTime endAt = LocalDateTime.of(2026, 8, 1, 10, 0);

        assertThatThrownBy(() -> ShopSuspension.of(ShopId.of(1L), SuspensionReason.BAD_WEATHER, OrderMethod.DELIVERY, startAt, endAt))
            .isInstanceOf(BusinessException.class)
            .extracting(exception -> ((BusinessException) exception).getErrorCode())
            .isEqualTo(ErrorCode.SHOP_SUSPENSION_INVALID_PERIOD);
    }

    @Test
    @DisplayName("release 후에는 releasedAt이 설정되어 isActive가 false를 반환한다")
    void release_setsReleasedAt_andIsActiveReturnsFalse() {
        LocalDateTime startAt = LocalDateTime.of(2026, 8, 1, 10, 0);
        LocalDateTime endAt = LocalDateTime.of(2026, 8, 1, 12, 0);
        ShopSuspension shopSuspension = ShopSuspension.of(ShopId.of(1L), SuspensionReason.BAD_WEATHER, OrderMethod.DELIVERY, startAt, endAt);
        LocalDateTime releasedAt = LocalDateTime.of(2026, 8, 1, 11, 0);

        shopSuspension.release(releasedAt);

        assertThat(shopSuspension.getReleasedAt()).isEqualTo(releasedAt);
        assertThat(shopSuspension.isActive(releasedAt)).isFalse();
    }

    @Test
    @DisplayName("해제되지 않고 기간 내인 경우 isActive는 true를 반환한다")
    void isActive_returnsTrue_whenWithinPeriodAndNotReleased() {
        LocalDateTime startAt = LocalDateTime.of(2026, 8, 1, 10, 0);
        LocalDateTime endAt = LocalDateTime.of(2026, 8, 1, 12, 0);
        ShopSuspension shopSuspension = ShopSuspension.of(ShopId.of(1L), SuspensionReason.BAD_WEATHER, OrderMethod.DELIVERY, startAt, endAt);

        assertThat(shopSuspension.isActive(LocalDateTime.of(2026, 8, 1, 11, 0))).isTrue();
    }

    @Test
    @DisplayName("reconstitute는 DB 상태로부터 식별자·감사 시각·해제 시각을 포함해 재구성한다")
    void reconstitute_restoresPersistedState() {
        LocalDateTime startAt = LocalDateTime.of(2026, 8, 1, 10, 0);
        LocalDateTime endAt = LocalDateTime.of(2026, 8, 1, 12, 0);
        LocalDateTime releasedAt = LocalDateTime.of(2026, 8, 1, 11, 0);
        LocalDateTime createdAt = LocalDateTime.of(2026, 7, 25, 10, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 7, 25, 10, 30);

        ShopSuspension shopSuspension = ShopSuspension.reconstitute(
            1L, ShopId.of(2L), SuspensionReason.UNREACHABLE, null, startAt, endAt, releasedAt, createdAt, updatedAt
        );

        assertThat(shopSuspension.getId()).isEqualTo(1L);
        assertThat(shopSuspension.getShopId()).isEqualTo(ShopId.of(2L));
        assertThat(shopSuspension.getReason()).isEqualTo(SuspensionReason.UNREACHABLE);
        assertThat(shopSuspension.getOrderMethod()).isNull();
        assertThat(shopSuspension.getReleasedAt()).isEqualTo(releasedAt);
        assertThat(shopSuspension.getCreatedAt()).isEqualTo(createdAt);
        assertThat(shopSuspension.getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    @DisplayName("전체 대상 중지(orderMethod=null)는 어떤 주문유형에도 적용된다")
    void appliesTo_returnsTrue_whenSuspensionTargetsAllOrderMethods() {
        ShopSuspension shopSuspension = suspension(null);

        assertThat(shopSuspension.appliesTo(OrderMethod.DELIVERY)).isTrue();
        assertThat(shopSuspension.appliesTo(OrderMethod.TAKEOUT)).isTrue();
        // target=null은 가게 전체 판정 — 전체 대상 중지는 가게 전체를 멈춘다
        assertThat(shopSuspension.appliesTo(null)).isTrue();
    }

    @Test
    @DisplayName("유형별 중지는 같은 유형에만 적용되고, 가게 전체 판정(target=null)에는 걸리지 않는다")
    void appliesTo_matchesOnlySameOrderMethod_andNeverShopWide() {
        ShopSuspension shopSuspension = suspension(OrderMethod.DELIVERY);

        assertThat(shopSuspension.appliesTo(OrderMethod.DELIVERY)).isTrue();
        assertThat(shopSuspension.appliesTo(OrderMethod.TAKEOUT)).isFalse();
        // 배달만 멈춰도 가게 전체는 멈추지 않는다 — 결함 A 수정의 핵심
        assertThat(shopSuspension.appliesTo(null)).isFalse();
    }

    @Test
    @DisplayName("해제된 중지는 유형이 일치해도 isActive(now, target)이 false다")
    void isActiveWithOrderMethod_returnsFalse_whenReleased() {
        ShopSuspension shopSuspension = suspension(OrderMethod.DELIVERY);
        shopSuspension.release(LocalDateTime.of(2026, 8, 1, 10, 30));

        assertThat(shopSuspension.isActive(LocalDateTime.of(2026, 8, 1, 11, 0), OrderMethod.DELIVERY)).isFalse();
    }

    @Test
    @DisplayName("기간 밖이면 유형이 일치해도 isActive(now, target)이 false다")
    void isActiveWithOrderMethod_returnsFalse_whenOutsidePeriod() {
        ShopSuspension shopSuspension = suspension(OrderMethod.DELIVERY);

        assertThat(shopSuspension.isActive(LocalDateTime.of(2026, 8, 1, 13, 0), OrderMethod.DELIVERY)).isFalse();
    }

    @Test
    @DisplayName("기간 내이고 유형이 일치하면 isActive(now, target)이 true다")
    void isActiveWithOrderMethod_returnsTrue_whenWithinPeriodAndMatchingOrderMethod() {
        ShopSuspension shopSuspension = suspension(OrderMethod.DELIVERY);

        assertThat(shopSuspension.isActive(LocalDateTime.of(2026, 8, 1, 11, 0), OrderMethod.DELIVERY)).isTrue();
        assertThat(shopSuspension.isActive(LocalDateTime.of(2026, 8, 1, 11, 0), OrderMethod.TAKEOUT)).isFalse();
        assertThat(shopSuspension.isActive(LocalDateTime.of(2026, 8, 1, 11, 0), null)).isFalse();
    }

    /** 2026-08-01 10:00 ~ 12:00 활성 중지. */
    private ShopSuspension suspension(OrderMethod orderMethod) {
        return ShopSuspension.of(
            ShopId.of(1L),
            SuspensionReason.BAD_WEATHER,
            orderMethod,
            LocalDateTime.of(2026, 8, 1, 10, 0),
            LocalDateTime.of(2026, 8, 1, 12, 0)
        );
    }

    @Test
    @DisplayName("SuspensionReason.from은 유효한 코드로 enum 상수를 반환한다")
    void suspensionReasonFrom_returnsEnumConstant_forValidCode() {
        assertThat(SuspensionReason.from("EARLY_CLOSE")).isEqualTo(SuspensionReason.EARLY_CLOSE);
    }

    @Test
    @DisplayName("SuspensionReason.from은 알 수 없는 코드에 대해 예외를 던진다")
    void suspensionReasonFrom_throwsException_forUnknownCode() {
        assertThatThrownBy(() -> SuspensionReason.from("UNKNOWN_CODE"))
            .isInstanceOf(BusinessException.class)
            .extracting(exception -> ((BusinessException) exception).getErrorCode())
            .isEqualTo(ErrorCode.SHOP_SUSPENSION_REASON_UNKNOWN);
    }
}
