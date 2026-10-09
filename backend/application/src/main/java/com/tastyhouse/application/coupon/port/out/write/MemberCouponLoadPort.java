package com.tastyhouse.application.coupon.port.out.write;

import java.util.Optional;

import com.tastyhouse.domain.coupon.model.MemberCoupon;
import com.tastyhouse.domain.coupon.vo.CouponId;
import com.tastyhouse.domain.coupon.vo.MemberCouponId;
import com.tastyhouse.domain.member.vo.MemberId;

public interface MemberCouponLoadPort {

    Optional<MemberCoupon> findById(MemberCouponId id);

    boolean existsByMemberIdAndCouponId(MemberId memberId, CouponId couponId);
}
