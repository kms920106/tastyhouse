package com.tastyhouse.infrastructure.coupon.persistence;

import com.tastyhouse.domain.coupon.model.Coupon;

final class CouponMapper {
    private CouponMapper() {
    }

    static Coupon toDomain(CouponJpaEntity entity) {
        return Coupon.reconstitute(
            entity.getId(),
            entity.getName(),
            entity.getDescription(),
            entity.getDiscountType(),
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

    static CouponJpaEntity toEntity(Coupon domain) {
        return CouponJpaEntity.create(
            domain.getName(),
            domain.getDescription(),
            domain.getDiscountType(),
            domain.getDiscountAmount(),
            domain.getMaxDiscountAmount(),
            domain.getMinOrderAmount(),
            domain.getMaxDiscountCount(),
            domain.getIssueStartAt(),
            domain.getIssueEndAt(),
            domain.getUseStartAt(),
            domain.getUseEndAt(),
            domain.isVisible(),
            domain.isDeleted()
        );
    }

    static void applyChanges(CouponJpaEntity entity, Coupon domain) {
        entity.applyChanges(
            domain.getName(),
            domain.getDescription(),
            domain.getDiscountType(),
            domain.getDiscountAmount(),
            domain.getMaxDiscountAmount(),
            domain.getMinOrderAmount(),
            domain.getMaxDiscountCount(),
            domain.getIssueStartAt(),
            domain.getIssueEndAt(),
            domain.getUseStartAt(),
            domain.getUseEndAt(),
            domain.isVisible(),
            domain.isDeleted()
        );
    }
}
