package com.tastyhouse.infrastructure.coupon.persistence;

import com.tastyhouse.application.coupon.port.out.write.CouponState;

final class CouponMapper {
    private CouponMapper() {
    }

    static CouponState toState(CouponJpaEntity entity) {
        return new CouponState(
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

    static CouponJpaEntity toEntity(CouponState state) {
        return CouponJpaEntity.create(
            state.name(),
            state.description(),
            state.discountType(),
            state.discountAmount(),
            state.maxDiscountAmount(),
            state.minOrderAmount(),
            state.maxDiscountCount(),
            state.issueStartAt(),
            state.issueEndAt(),
            state.useStartAt(),
            state.useEndAt(),
            state.visible(),
            state.deleted()
        );
    }

    static void applyChanges(CouponJpaEntity entity, CouponState state) {
        entity.applyChanges(
            state.name(),
            state.description(),
            state.discountType(),
            state.discountAmount(),
            state.maxDiscountAmount(),
            state.minOrderAmount(),
            state.maxDiscountCount(),
            state.issueStartAt(),
            state.issueEndAt(),
            state.useStartAt(),
            state.useEndAt(),
            state.visible(),
            state.deleted()
        );
    }
}
