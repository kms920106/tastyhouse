package com.tastyhouse.application.coupon.store;

import com.tastyhouse.application.coupon.port.out.write.MemberCouponState;
import com.tastyhouse.domain.coupon.model.MemberCoupon;
import com.tastyhouse.domain.coupon.vo.CouponId;
import com.tastyhouse.domain.member.vo.MemberId;

final class MemberCouponStateMapper {
    private MemberCouponStateMapper() {
    }

    static MemberCoupon toDomain(MemberCouponState state) {
        return MemberCoupon.reconstitute(
            state.id(),
            state.memberId() == null ? null : MemberId.of(state.memberId()),
            state.couponId() == null ? null : CouponId.of(state.couponId()),
            state.used(),
            state.usedAt(),
            state.expiredAt()
        );
    }

    static MemberCouponState toState(MemberCoupon memberCoupon) {
        return new MemberCouponState(
            memberCoupon.getId(),
            memberCoupon.getMemberId() == null ? null : memberCoupon.getMemberId().value(),
            memberCoupon.getCouponId() == null ? null : memberCoupon.getCouponId().value(),
            memberCoupon.isUsed(),
            memberCoupon.getUsedAt(),
            memberCoupon.getExpiredAt()
        );
    }
}
