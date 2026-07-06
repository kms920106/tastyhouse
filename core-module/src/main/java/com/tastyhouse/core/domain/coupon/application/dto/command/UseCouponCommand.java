package com.tastyhouse.core.domain.coupon.application.dto.command;

import com.tastyhouse.core.domain.coupon.domain.vo.MemberCouponId;

public record UseCouponCommand(
    MemberCouponId memberCouponId,
    Long memberId,
    int orderAmountAfterProductDiscount
) {

    public static UseCouponCommand of(
        Long memberCouponId,
        Long memberId,
        int orderAmountAfterProductDiscount
    ) {
        return new UseCouponCommand(MemberCouponId.of(memberCouponId), memberId, orderAmountAfterProductDiscount);
    }
}
