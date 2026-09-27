package com.tastyhouse.infrastructure.coupon.persistence;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.coupon.model.MemberCoupon;
import com.tastyhouse.domain.coupon.vo.CouponId;
import com.tastyhouse.domain.member.vo.MemberId;

import static org.assertj.core.api.Assertions.assertThat;

class MemberCouponMapperTest {

    @Test
    @DisplayName("MemberCoupon → 엔티티 변환 시 VO는 value로 컬럼에 채워진다")
    void toEntity() {
        MemberCoupon memberCoupon = MemberCoupon.reconstitute(
            51L, MemberId.of(52L), CouponId.of(53L), true,
            LocalDateTime.of(2026, 7, 1, 0, 0),
            LocalDateTime.of(2026, 8, 1, 0, 0));

        MemberCouponJpaEntity entity = MemberCouponMapper.toEntity(memberCoupon);

        assertThat(entity.getMemberId()).isEqualTo(52L);
        assertThat(entity.getCouponId()).isEqualTo(53L);
        assertThat(entity.isUsed()).isTrue();
        assertThat(entity.getUsedAt()).isEqualTo(LocalDateTime.of(2026, 7, 1, 0, 0));
        assertThat(entity.getExpiredAt()).isEqualTo(LocalDateTime.of(2026, 8, 1, 0, 0));
    }

    @Test
    @DisplayName("엔티티 → MemberCoupon 변환 시 id를 포함한 모든 필드가 복원된다")
    void toDomain() {
        MemberCouponJpaEntity entity = MemberCouponJpaEntity.create(
            52L, 53L, true,
            LocalDateTime.of(2026, 7, 1, 0, 0),
            LocalDateTime.of(2026, 8, 1, 0, 0));
        ReflectionTestUtils.setField(entity, "id", 51L);
        ReflectionTestUtils.setField(entity, "createdAt", LocalDateTime.of(2026, 6, 1, 0, 0));
        ReflectionTestUtils.setField(entity, "updatedAt", LocalDateTime.of(2026, 6, 2, 0, 0));

        MemberCoupon memberCoupon = MemberCouponMapper.toDomain(entity);

        MemberCoupon expected = MemberCoupon.reconstitute(
            51L, MemberId.of(52L), CouponId.of(53L), true,
            LocalDateTime.of(2026, 7, 1, 0, 0),
            LocalDateTime.of(2026, 8, 1, 0, 0));
        assertThat(memberCoupon).usingRecursiveComparison().isEqualTo(expected);
    }
}
