package com.tastyhouse.application.coupon.store;

import java.util.Optional;

import com.tastyhouse.domain.coupon.model.MemberCoupon;
import com.tastyhouse.domain.coupon.vo.CouponId;
import com.tastyhouse.domain.coupon.vo.MemberCouponId;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.application.coupon.port.out.write.MemberCouponStatePort;

public class MemberCouponStore implements MemberCouponRepository {
    private final MemberCouponStatePort memberCouponStatePort;

    public MemberCouponStore(MemberCouponStatePort memberCouponStatePort) {
        this.memberCouponStatePort = memberCouponStatePort;
    }

    @Override
    public Optional<MemberCoupon> findById(MemberCouponId id) {
        return memberCouponStatePort.findById(id.value()).map(MemberCouponStateMapper::toDomain);
    }

    @Override
    public boolean existsByMemberIdAndCouponId(MemberId memberId, CouponId couponId) {
        return memberCouponStatePort.existsByMemberIdAndCouponId(memberId.value(), couponId.value());
    }

    @Override
    public MemberCoupon save(MemberCoupon memberCoupon) {
        return MemberCouponStateMapper.toDomain(memberCouponStatePort.save(MemberCouponStateMapper.toState(memberCoupon)));
    }
}
