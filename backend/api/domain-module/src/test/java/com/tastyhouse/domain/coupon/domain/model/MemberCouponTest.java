package com.tastyhouse.domain.coupon.domain.model;

import java.time.LocalDateTime;

import com.tastyhouse.domain.coupon.model.MemberCoupon;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tastyhouse.domain.coupon.vo.CouponId;
import com.tastyhouse.domain.coupon.vo.MemberCouponId;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.exception.BusinessException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 순수 도메인 모델 단위 테스트. Spring/JPA 컨텍스트 없이 도메인 로직만 검증한다
 * (도메인/JPA 엔티티 분리로 얻는 테스트 용이성의 레퍼런스).
 */
class MemberCouponTest {

    @Test
    @DisplayName("of로 생성하면 미영속 상태(식별자 없음)다")
    void of_createsTransientMemberCoupon() {
        MemberCoupon memberCoupon = MemberCoupon.of(
            MemberId.of(1L), CouponId.of(10L), false, null, LocalDateTime.now().plusDays(7)
        );

        assertThat(memberCoupon.getId()).isNull();
        assertThat(memberCoupon.getMemberId()).isEqualTo(MemberId.of(1L));
        assertThat(memberCoupon.getCouponId()).isEqualTo(CouponId.of(10L));
        assertThat(memberCoupon.isUsed()).isFalse();
    }

    @Test
    @DisplayName("use는 사용 가능한 쿠폰을 사용 처리하고 사용 시각을 기록한다")
    void use_marksUsed() {
        MemberCoupon memberCoupon = MemberCoupon.of(
            MemberId.of(1L), CouponId.of(10L), false, null, LocalDateTime.now().plusDays(7)
        );

        memberCoupon.use();

        assertThat(memberCoupon.isUsed()).isTrue();
        assertThat(memberCoupon.getUsedAt()).isNotNull();
    }

    @Test
    @DisplayName("이미 사용된 쿠폰을 다시 use하면 예외가 발생한다")
    void use_onAlreadyUsed_throws() {
        MemberCoupon memberCoupon = MemberCoupon.of(
            MemberId.of(1L), CouponId.of(10L), false, null, LocalDateTime.now().plusDays(7)
        );
        memberCoupon.use();

        assertThatThrownBy(memberCoupon::use)
            .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("만료된 쿠폰을 use하면 예외가 발생한다")
    void use_onExpired_throws() {
        MemberCoupon memberCoupon = MemberCoupon.of(
            MemberId.of(1L), CouponId.of(10L), false, null, LocalDateTime.now().minusDays(1)
        );

        assertThatThrownBy(memberCoupon::use)
            .isInstanceOf(BusinessException.class);
    }

    @Test
    @DisplayName("isExpired/isAvailable은 만료 시각과 사용 여부로 판단한다")
    void isExpiredAndIsAvailable_reflectState() {
        MemberCoupon available = MemberCoupon.of(
            MemberId.of(1L), CouponId.of(10L), false, null, LocalDateTime.now().plusDays(1)
        );
        MemberCoupon expired = MemberCoupon.of(
            MemberId.of(1L), CouponId.of(10L), false, null, LocalDateTime.now().minusDays(1)
        );

        assertThat(available.isExpired()).isFalse();
        assertThat(available.isAvailable()).isTrue();
        assertThat(expired.isExpired()).isTrue();
        assertThat(expired.isAvailable()).isFalse();
    }

    @Test
    @DisplayName("reconstitute는 DB 상태로부터 식별자를 포함해 재구성한다")
    void reconstitute_restoresPersistedState() {
        LocalDateTime usedAt = LocalDateTime.of(2026, 1, 2, 0, 0);
        LocalDateTime expiredAt = LocalDateTime.of(2026, 2, 1, 0, 0);

        MemberCoupon memberCoupon = MemberCoupon.reconstitute(
            1L, MemberId.of(2L), CouponId.of(10L), true, usedAt, expiredAt
        );

        assertThat(memberCoupon.getId()).isEqualTo(1L);
        assertThat(memberCoupon.getMemberCouponId()).isEqualTo(MemberCouponId.of(1L));
        assertThat(memberCoupon.isUsed()).isTrue();
        assertThat(memberCoupon.getUsedAt()).isEqualTo(usedAt);
        assertThat(memberCoupon.getExpiredAt()).isEqualTo(expiredAt);
    }

    @Test
    @DisplayName("expiredAt이 null이면 isExpired가 NPE 없이 false(무기한)를 반환한다")
    void isExpired_withNullExpiredAt_returnsFalseWithoutNpe() {
        MemberCoupon unlimited = MemberCoupon.of(MemberId.of(1L), CouponId.of(10L), false, null, null);

        assertThat(unlimited.isExpired()).isFalse();
        assertThat(unlimited.isAvailable()).isTrue();
    }

    @Test
    @DisplayName("expiredAt이 null인 레거시 행을 reconstitute해도 만료 판정이 NPE 없이 동작한다")
    void isExpired_withNullExpiredAtOnReconstituted_doesNotThrow() {
        MemberCoupon legacy = MemberCoupon.reconstitute(1L, MemberId.of(2L), CouponId.of(10L), false, null, null);

        assertThat(legacy.isExpired()).isFalse();
        assertThat(legacy.isAvailable()).isTrue();
    }

    @Test
    @DisplayName("expiredAt이 null이면 무기한이므로 use가 만료로 막히지 않는다")
    void use_withNullExpiredAt_succeeds() {
        MemberCoupon unlimited = MemberCoupon.of(MemberId.of(1L), CouponId.of(10L), false, null, null);

        unlimited.use();

        assertThat(unlimited.isUsed()).isTrue();
    }
}
