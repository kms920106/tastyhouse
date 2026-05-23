package com.tastyhouse.core.domain.coupon.application.dto.command;

public record UseCouponCommand(
    Long memberCouponId,
    Long memberId,
    int orderAmountAfterProductDiscount
) {
}
