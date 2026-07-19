package com.tastyhouse.core.domain.coupon.domain.model;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.core.domain.coupon.domain.vo.CouponId;
import com.tastyhouse.core.exception.BusinessException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 순수 도메인 모델 단위 테스트. Spring/JPA 컨텍스트 없이 도메인 로직만 검증한다
 * (도메인/JPA 엔티티 분리로 얻는 테스트 용이성의 레퍼런스).
 */
class CouponTest {

    @Test
    @DisplayName("of로 생성하면 미영속 상태(식별자·감사시각 없음)이고 삭제되지 않은 상태다")
    void of_createsTransientCoupon() {
        Coupon coupon = Coupon.of(
            "쿠폰명", "설명", DiscountType.AMOUNT, 1000, null, 5000, null,
            LocalDateTime.of(2026, 1, 1, 0, 0), LocalDateTime.of(2026, 1, 31, 0, 0),
            LocalDateTime.of(2026, 1, 1, 0, 0), LocalDateTime.of(2026, 2, 28, 0, 0),
            true
        );

        assertThat(coupon.getId()).isNull();
        assertThat(coupon.getName()).isEqualTo("쿠폰명");
        assertThat(coupon.isDeleted()).isFalse();
        assertThat(coupon.getCreatedAt()).isNull();
        assertThat(coupon.getUpdatedAt()).isNull();
    }

    @Test
    @DisplayName("of에서 discountType이 null이면 AMOUNT로, minOrderAmount가 null이면 0으로 정규화한다")
    void of_normalizesDefaults() {
        Coupon coupon = Coupon.of(
            "쿠폰명", "설명", null, 1000, null, null, null,
            LocalDateTime.now(), LocalDateTime.now().plusDays(1),
            LocalDateTime.now(), LocalDateTime.now().plusDays(1),
            true
        );

        assertThat(coupon.getDiscountType()).isEqualTo(DiscountType.AMOUNT);
        assertThat(coupon.getMinOrderAmount()).isEqualTo(0);
    }

    @Test
    @DisplayName("update는 값 필드를 변경하고 null discountType/minOrderAmount는 기본값으로 정규화한다")
    void update_changesFieldsAndNormalizes() {
        Coupon coupon = Coupon.of(
            "쿠폰명", "설명", DiscountType.AMOUNT, 1000, null, 5000, null,
            LocalDateTime.now(), LocalDateTime.now().plusDays(1),
            LocalDateTime.now(), LocalDateTime.now().plusDays(1),
            true
        );

        coupon.update(
            "새 쿠폰명", "새 설명", null, 2000, 3000, null, 10,
            LocalDateTime.now(), LocalDateTime.now().plusDays(2),
            LocalDateTime.now(), LocalDateTime.now().plusDays(2),
            false
        );

        assertThat(coupon.getName()).isEqualTo("새 쿠폰명");
        assertThat(coupon.getDiscountType()).isEqualTo(DiscountType.AMOUNT);
        assertThat(coupon.getMinOrderAmount()).isEqualTo(0);
        assertThat(coupon.getMaxDiscountCount()).isEqualTo(10);
        assertThat(coupon.isVisible()).isFalse();
    }

    @Test
    @DisplayName("delete는 삭제 플래그를 true로 만든다(soft delete)")
    void delete_marksDeleted() {
        Coupon coupon = Coupon.of(
            "쿠폰명", "설명", DiscountType.AMOUNT, 1000, null, 5000, null,
            LocalDateTime.now(), LocalDateTime.now().plusDays(1),
            LocalDateTime.now(), LocalDateTime.now().plusDays(1),
            true
        );

        coupon.delete();

        assertThat(coupon.isDeleted()).isTrue();
    }

    @Test
    @DisplayName("reconstitute는 DB 상태로부터 식별자·감사시각을 포함해 재구성한다")
    void reconstitute_restoresPersistedState() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 0, 0);
        LocalDateTime updatedAt = LocalDateTime.of(2026, 1, 2, 0, 0);

        Coupon coupon = Coupon.reconstitute(
            1L, "쿠폰명", "설명", DiscountType.AMOUNT, 1000, null, 5000, null,
            LocalDateTime.now(), LocalDateTime.now().plusDays(1),
            LocalDateTime.now(), LocalDateTime.now().plusDays(1),
            true, false, createdAt, updatedAt
        );

        assertThat(coupon.getId()).isEqualTo(1L);
        assertThat(coupon.getCouponId()).isEqualTo(CouponId.of(1L));
        assertThat(coupon.getCreatedAt()).isEqualTo(createdAt);
        assertThat(coupon.getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    @DisplayName("calculateDiscount는 AMOUNT면 정액을, RATE면 정률(최대 한도 이하)을 계산한다")
    void calculateDiscount_calculatesByDiscountType() {
        Coupon amountCoupon = Coupon.of(
            "정액쿠폰", "설명", DiscountType.AMOUNT, 1000, null, 0, null,
            LocalDateTime.now(), LocalDateTime.now().plusDays(1),
            LocalDateTime.now(), LocalDateTime.now().plusDays(1),
            true
        );
        Coupon rateCoupon = Coupon.of(
            "정률쿠폰", "설명", DiscountType.RATE, 10, 3000, 0, null,
            LocalDateTime.now(), LocalDateTime.now().plusDays(1),
            LocalDateTime.now(), LocalDateTime.now().plusDays(1),
            true
        );

        assertThat(amountCoupon.calculateDiscount(50000)).isEqualTo(1000);
        assertThat(rateCoupon.calculateDiscount(50000)).isEqualTo(3000); // 10% = 5000, capped at 3000
        assertThat(rateCoupon.calculateDiscount(10000)).isEqualTo(1000); // 10% = 1000, under cap
    }

    @Test
    @DisplayName("validateMinOrderAmount는 최소 주문 금액 미달 시 예외를 던진다")
    void validateMinOrderAmount_throwsWhenBelowMinimum() {
        Coupon coupon = Coupon.of(
            "쿠폰명", "설명", DiscountType.AMOUNT, 1000, null, 5000, null,
            LocalDateTime.now(), LocalDateTime.now().plusDays(1),
            LocalDateTime.now(), LocalDateTime.now().plusDays(1),
            true
        );

        assertThatThrownBy(() -> coupon.validateMinOrderAmount(1000))
            .isInstanceOf(BusinessException.class);

        assertThat(coupon).satisfies(c -> c.validateMinOrderAmount(5000)); // 경계값은 통과
    }
}
