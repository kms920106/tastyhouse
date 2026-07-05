package com.tastyhouse.core.domain.coupon.application.dto.command;

public record UseCouponCommand(
    Long memberCouponId,
    Long memberId,
    int orderAmountAfterProductDiscount
) {

    public static UseCouponCommand of(
        Long memberCouponId,
        Long memberId,
        int orderAmountAfterProductDiscount
    ) {
        return new UseCouponCommand(memberCouponId, memberId, orderAmountAfterProductDiscount);
    }
}
