package com.tastyhouse.application.coupon.store;

import com.tastyhouse.domain.coupon.model.Coupon;
import com.tastyhouse.domain.coupon.model.DiscountType;
import com.tastyhouse.application.coupon.port.out.write.CouponState;

final class CouponStateMapper {
    private CouponStateMapper() {
    }

    static Coupon toDomain(CouponState state) {
        return Coupon.reconstitute(
            state.id(),
            state.name(),
            state.description(),
            state.discountType() == null ? null : DiscountType.valueOf(state.discountType()),
            state.discountAmount(),
            state.maxDiscountAmount(),
            state.minOrderAmount(),
            state.maxDiscountCount(),
            state.issueStartAt(),
            state.issueEndAt(),
            state.useStartAt(),
            state.useEndAt(),
            state.visible(),
            state.deleted(),
            state.createdAt(),
            state.updatedAt()
        );
    }

    static CouponState toState(Coupon coupon) {
        return new CouponState(
            coupon.getId(),
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
            coupon.isDeleted(),
            coupon.getCreatedAt(),
            coupon.getUpdatedAt()
        );
    }
}
