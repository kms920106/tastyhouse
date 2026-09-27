package com.tastyhouse.application.coupon.port.out.write;

import java.util.Optional;

public interface MemberCouponStatePort {
    Optional<MemberCouponState> findById(Long id);

    boolean existsByMemberIdAndCouponId(Long memberId, Long couponId);

    MemberCouponState save(MemberCouponState state);
}
