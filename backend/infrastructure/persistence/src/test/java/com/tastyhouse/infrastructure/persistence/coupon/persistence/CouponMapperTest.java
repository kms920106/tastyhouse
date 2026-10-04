package com.tastyhouse.infrastructure.persistence.coupon.persistence;

import java.time.LocalDateTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.tastyhouse.domain.coupon.model.Coupon;
import com.tastyhouse.domain.coupon.model.DiscountType;

import static org.assertj.core.api.Assertions.assertThat;

class CouponMapperTest {

    @Test
    @DisplayName("Coupon → 엔티티 변환 시 enum은 name으로, 나머지는 그대로 컬럼에 채워진다")
    void toEntity() {
        Coupon coupon = Coupon.reconstitute(
            41L, "쿠폰명", "설명", DiscountType.RATE, 10, 5000, 20000, 100,
            LocalDateTime.of(2026, 1, 1, 0, 0),
            LocalDateTime.of(2026, 2, 1, 0, 0),
            LocalDateTime.of(2026, 3, 1, 0, 0),
            LocalDateTime.of(2026, 4, 1, 0, 0),
            true, false,
            LocalDateTime.of(2026, 5, 1, 0, 0),
            LocalDateTime.of(2026, 6, 1, 0, 0));

        CouponJpaEntity entity = CouponMapper.toEntity(coupon);

        assertThat(entity.getName()).isEqualTo("쿠폰명");
        assertThat(entity.getDescription()).isEqualTo("설명");
        assertThat(entity.getDiscountType()).isEqualTo("RATE");
        assertThat(entity.getDiscountAmount()).isEqualTo(10);
        assertThat(entity.getMaxDiscountAmount()).isEqualTo(5000);
        assertThat(entity.getMinOrderAmount()).isEqualTo(20000);
        assertThat(entity.getMaxDiscountCount()).isEqualTo(100);
        assertThat(entity.getIssueStartAt()).isEqualTo(LocalDateTime.of(2026, 1, 1, 0, 0));
        assertThat(entity.getIssueEndAt()).isEqualTo(LocalDateTime.of(2026, 2, 1, 0, 0));
        assertThat(entity.getUseStartAt()).isEqualTo(LocalDateTime.of(2026, 3, 1, 0, 0));
        assertThat(entity.getUseEndAt()).isEqualTo(LocalDateTime.of(2026, 4, 1, 0, 0));
        assertThat(entity.isVisible()).isTrue();
        assertThat(entity.isDeleted()).isFalse();
    }

    @Test
    @DisplayName("엔티티 → Coupon 변환 시 id·생성/수정 시각을 포함한 모든 필드가 복원된다")
    void toDomain() {
        CouponJpaEntity entity = CouponJpaEntity.create(
            "쿠폰명", "설명", "RATE", 10, 5000, 20000, 100,
            LocalDateTime.of(2026, 1, 1, 0, 0),
            LocalDateTime.of(2026, 2, 1, 0, 0),
            LocalDateTime.of(2026, 3, 1, 0, 0),
            LocalDateTime.of(2026, 4, 1, 0, 0),
            true, false);
        ReflectionTestUtils.setField(entity, "id", 41L);
        ReflectionTestUtils.setField(entity, "createdAt", LocalDateTime.of(2026, 5, 1, 0, 0));
        ReflectionTestUtils.setField(entity, "updatedAt", LocalDateTime.of(2026, 6, 1, 0, 0));

        Coupon coupon = CouponMapper.toDomain(entity);

        Coupon expected = Coupon.reconstitute(
            41L, "쿠폰명", "설명", DiscountType.RATE, 10, 5000, 20000, 100,
            LocalDateTime.of(2026, 1, 1, 0, 0),
            LocalDateTime.of(2026, 2, 1, 0, 0),
            LocalDateTime.of(2026, 3, 1, 0, 0),
            LocalDateTime.of(2026, 4, 1, 0, 0),
            true, false,
            LocalDateTime.of(2026, 5, 1, 0, 0),
            LocalDateTime.of(2026, 6, 1, 0, 0));
        assertThat(coupon).usingRecursiveComparison().isEqualTo(expected);
    }
}
