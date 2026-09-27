package com.tastyhouse.application.coupon.store;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.coupon.model.Coupon;
import com.tastyhouse.domain.coupon.model.DiscountType;
import com.tastyhouse.domain.coupon.model.MemberCoupon;
import com.tastyhouse.domain.coupon.vo.CouponId;
import com.tastyhouse.domain.member.vo.MemberId;

import static org.assertj.core.api.Assertions.assertThat;

class CouponStateMapperTest {

    @Test
    @DisplayName("Coupon → CouponState → Coupon 왕복 시 모든 필드가 보존된다")
    void couponRoundTrip() {
        Coupon original = Coupon.reconstitute(
            41L, "쿠폰명", "설명", DiscountType.RATE, 10, 5000, 20000, 100,
            LocalDateTime.of(2026, 1, 1, 0, 0),
            LocalDateTime.of(2026, 2, 1, 0, 0),
            LocalDateTime.of(2026, 3, 1, 0, 0),
            LocalDateTime.of(2026, 4, 1, 0, 0),
            true, false,
            LocalDateTime.of(2026, 5, 1, 0, 0),
            LocalDateTime.of(2026, 6, 1, 0, 0));

        Coupon restored = CouponStateMapper.toDomain(CouponStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }

    @Test
    @DisplayName("MemberCoupon → MemberCouponState → MemberCoupon 왕복 시 모든 필드가 보존된다")
    void memberCouponRoundTrip() {
        MemberCoupon original = MemberCoupon.reconstitute(
            51L, MemberId.of(52L), CouponId.of(53L), true,
            LocalDateTime.of(2026, 7, 1, 0, 0),
            LocalDateTime.of(2026, 8, 1, 0, 0));

        MemberCoupon restored = MemberCouponStateMapper.toDomain(MemberCouponStateMapper.toState(original));

        assertThat(restored).usingRecursiveComparison().isEqualTo(original);
    }
}
