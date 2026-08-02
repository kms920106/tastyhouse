package com.tastyhouse.domain.coupon.domain.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.coupon.model.Coupon;
import com.tastyhouse.domain.coupon.model.DiscountType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.coupon.vo.CouponId;
import com.tastyhouse.domain.exception.BusinessException;
import com.tastyhouse.domain.exception.ErrorCode;

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

    private static final LocalDateTime START = LocalDateTime.of(2026, 1, 1, 0, 0);
    private static final LocalDateTime END = LocalDateTime.of(2026, 1, 31, 0, 0);

    private static Coupon couponOf(DiscountType discountType, Integer discountAmount) {
        return Coupon.of("쿠폰명", "설명", discountType, discountAmount, null, 0, null,
            START, END, START, END, true);
    }

    private static Coupon couponWithPeriod(
        LocalDateTime issueStartAt,
        LocalDateTime issueEndAt,
        LocalDateTime useStartAt,
        LocalDateTime useEndAt
    ) {
        return Coupon.of("쿠폰명", "설명", DiscountType.AMOUNT, 1000, null, 0, null,
            issueStartAt, issueEndAt, useStartAt, useEndAt, true);
    }

    @Test
    @DisplayName("of는 RATE 할인율 경계값 1과 100을 통과시킨다")
    void of_rateBoundaries_pass() {
        assertThat(couponOf(DiscountType.RATE, 1).getDiscountAmount()).isEqualTo(1);
        assertThat(couponOf(DiscountType.RATE, 100).getDiscountAmount()).isEqualTo(100);
    }

    @Test
    @DisplayName("of는 RATE 할인율이 0 이하이거나 100 초과이면 COUPON_DISCOUNT_RATE_INVALID로 거부한다")
    void of_rateOutOfRange_throws() {
        assertThatThrownBy(() -> couponOf(DiscountType.RATE, 0))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.COUPON_DISCOUNT_RATE_INVALID);

        assertThatThrownBy(() -> couponOf(DiscountType.RATE, 101))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.COUPON_DISCOUNT_RATE_INVALID);

        // 문제 상세에 적힌 대표 사례: RATE인데 200(=200%)
        assertThatThrownBy(() -> couponOf(DiscountType.RATE, 200))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.COUPON_DISCOUNT_RATE_INVALID);
    }

    @Test
    @DisplayName("of는 AMOUNT 할인 금액 경계값 1을 통과시키고 0 이하는 거부한다")
    void of_amountBoundary_passesAtOneAndRejectsBelow() {
        assertThat(couponOf(DiscountType.AMOUNT, 1).getDiscountAmount()).isEqualTo(1);

        assertThatThrownBy(() -> couponOf(DiscountType.AMOUNT, 0))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.COUPON_DISCOUNT_AMOUNT_INVALID);

        assertThatThrownBy(() -> couponOf(DiscountType.AMOUNT, null))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.COUPON_DISCOUNT_AMOUNT_INVALID);
    }

    @Test
    @DisplayName("of는 발급/사용 기간의 시작이 종료보다 늦으면 COUPON_PERIOD_INVALID로 거부한다")
    void of_reversedPeriod_throws() {
        assertThatThrownBy(() -> couponWithPeriod(END, START, START, END))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.COUPON_PERIOD_INVALID);

        assertThatThrownBy(() -> couponWithPeriod(START, END, END, START))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.COUPON_PERIOD_INVALID);
    }

    @Test
    @DisplayName("of는 시작과 종료가 같은 기간(경계값)을 통과시킨다")
    void of_sameStartAndEnd_passes() {
        assertThat(couponWithPeriod(START, START, START, START).getUseEndAt()).isEqualTo(START);
    }

    @Test
    @DisplayName("of는 useEndAt이 null이면 COUPON_USE_END_AT_REQUIRED로 거부한다")
    void of_nullUseEndAt_throws() {
        assertThatThrownBy(() -> couponWithPeriod(START, END, START, null))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.COUPON_USE_END_AT_REQUIRED);
    }

    @Test
    @DisplayName("of는 음수 금액(최대 할인·최소 주문)을 COUPON_AMOUNT_NEGATIVE로 거부한다")
    void of_negativeAmounts_throw() {
        assertThatThrownBy(() -> Coupon.of("쿠폰명", "설명", DiscountType.AMOUNT, 1000, -1, 0, null,
            START, END, START, END, true))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.COUPON_AMOUNT_NEGATIVE);

        assertThatThrownBy(() -> Coupon.of("쿠폰명", "설명", DiscountType.AMOUNT, 1000, null, -1, null,
            START, END, START, END, true))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.COUPON_AMOUNT_NEGATIVE);
    }

    @Test
    @DisplayName("update도 of와 같은 값 불변식을 강제한다(생성만 막고 변경을 열어두지 않는다)")
    void update_enforcesSameInvariants() {
        Coupon coupon = couponOf(DiscountType.AMOUNT, 1000);

        assertThatThrownBy(() -> coupon.update("쿠폰명", "설명", DiscountType.RATE, 200, null, 0, null,
            START, END, START, END, true))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.COUPON_DISCOUNT_RATE_INVALID);

        // 실패한 update는 기존 상태를 바꾸지 않는다
        assertThat(coupon.getDiscountType()).isEqualTo(DiscountType.AMOUNT);
        assertThat(coupon.getDiscountAmount()).isEqualTo(1000);
    }

    @Test
    @DisplayName("reconstitute는 값 불변식 검증을 하지 않는다(불변식 위반 레거시 행도 로드 가능)")
    void reconstitute_bypassesValueValidation() {
        Coupon coupon = Coupon.reconstitute(
            1L, "레거시쿠폰", "설명", DiscountType.RATE, 200, -1, -1, null,
            END, START, END, null,
            true, false, null, null
        );

        assertThat(coupon.getDiscountAmount()).isEqualTo(200);
        assertThat(coupon.getUseEndAt()).isNull();
    }
}
