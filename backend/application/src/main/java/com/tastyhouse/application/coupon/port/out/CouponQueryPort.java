package com.tastyhouse.application.coupon.port.out;

import java.time.LocalDateTime;
import java.util.List;

public interface CouponQueryPort {

    List<MemberCouponResult> findMemberCoupons(Long memberId);

    List<MemberCouponResult> findAvailableMemberCoupons(Long memberId, LocalDateTime now);
}
