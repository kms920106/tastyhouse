package com.tastyhouse.core.domain.coupon.application.dto.command;

import com.tastyhouse.core.domain.coupon.domain.vo.MemberCouponId;
import com.tastyhouse.core.domain.member.domain.vo.MemberId;

public record UseCouponCommand(
    MemberCouponId memberCouponId,
    MemberId memberId,
    int orderAmountAfterProductDiscount
) {

    public static UseCouponCommand of(
        Long memberCouponId,
        MemberId memberId,
        int orderAmountAfterProductDiscount
    ) {
        return new UseCouponCommand(MemberCouponId.of(memberCouponId), memberId, orderAmountAfterProductDiscount);
    }
}
