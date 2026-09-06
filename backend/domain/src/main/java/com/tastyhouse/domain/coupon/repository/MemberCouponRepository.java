package com.tastyhouse.domain.coupon.repository;

import java.util.Optional;

import com.tastyhouse.domain.coupon.model.MemberCoupon;
import com.tastyhouse.domain.coupon.vo.CouponId;
import com.tastyhouse.domain.coupon.vo.MemberCouponId;
import com.tastyhouse.domain.member.vo.MemberId;

public interface MemberCouponRepository {
    Optional<MemberCoupon> findById(MemberCouponId id);

    boolean existsByMemberIdAndCouponId(MemberId memberId, CouponId couponId);

    MemberCoupon save(MemberCoupon memberCoupon);
}
