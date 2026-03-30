package com.tastyhouse.core.repository.coupon;

import com.tastyhouse.core.entity.coupon.MemberCoupon;

import java.time.LocalDateTime;
import java.util.List;

public interface CouponRepository {

    List<MemberCoupon> findMemberCouponsByMemberId(Long memberId);

    List<MemberCoupon> findAvailableMemberCouponsByMemberId(Long memberId, LocalDateTime currentTime);

}
