package com.tastyhouse.core.domain.coupon.domain.repository;

import com.tastyhouse.core.domain.coupon.application.dto.result.MemberCouponResult;
import com.tastyhouse.core.domain.coupon.domain.model.MemberCoupon;
import com.tastyhouse.core.domain.coupon.domain.model.MemberCouponId;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface MemberCouponRepository {

    Optional<MemberCoupon> findById(MemberCouponId id);

    List<MemberCouponResult> findWithCouponByMemberId(Long memberId);

    List<MemberCouponResult> findAvailableWithCouponByMemberId(Long memberId, LocalDateTime now);

    MemberCoupon save(MemberCoupon memberCoupon);
}
