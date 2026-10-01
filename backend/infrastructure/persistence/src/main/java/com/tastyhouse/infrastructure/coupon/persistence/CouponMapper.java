package com.tastyhouse.infrastructure.coupon.persistence;

import com.tastyhouse.domain.coupon.model.Coupon;
import com.tastyhouse.domain.coupon.model.DiscountType;

final class CouponMapper {

    private CouponMapper() {
    }

    static Coupon toDomain(CouponJpaEntity entity) {
        return Coupon.reconstitute(
            entity.getId(),
            entity.getName(),
            entity.getDescription(),
            entity.getDiscountType() == null ? null : DiscountType.valueOf(entity.getDiscountType()),
            entity.getDiscountAmount(),
            entity.getMaxDiscountAmount(),
            entity.getMinOrderAmount(),
            entity.getMaxDiscountCount(),
            entity.getIssueStartAt(),
            entity.getIssueEndAt(),
            entity.getUseStartAt(),
            entity.getUseEndAt(),
            entity.isVisible(),
            entity.isDeleted(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    static CouponJpaEntity toEntity(Coupon coupon) {
        return CouponJpaEntity.create(
            coupon.getName(),
            coupon.getDescription(),
            coupon.getDiscountType() == null ? null : coupon.getDiscountType().name(),
            coupon.getDiscountAmount(),
            coupon.getMaxDiscountAmount(),
            coupon.getMinOrderAmount(),
            coupon.getMaxDiscountCount(),
            coupon.getIssueStartAt(),
            coupon.getIssueEndAt(),
            coupon.getUseStartAt(),
            coupon.getUseEndAt(),
            coupon.isVisible(),
            coupon.isDeleted()
        );
    }

    static void applyChanges(CouponJpaEntity entity, Coupon coupon) {
        entity.applyChanges(
            coupon.getName(),
            coupon.getDescription(),
            coupon.getDiscountType() == null ? null : coupon.getDiscountType().name(),
            coupon.getDiscountAmount(),
            coupon.getMaxDiscountAmount(),
            coupon.getMinOrderAmount(),
            coupon.getMaxDiscountCount(),
            coupon.getIssueStartAt(),
            coupon.getIssueEndAt(),
            coupon.getUseStartAt(),
            coupon.getUseEndAt(),
            coupon.isVisible(),
            coupon.isDeleted()
        );
    }
}
