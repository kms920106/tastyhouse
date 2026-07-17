package com.tastyhouse.core.domain.coupon.domain.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.tastyhouse.core.domain.coupon.domain.model.MemberCoupon;
import com.tastyhouse.core.domain.coupon.domain.vo.CouponId;
import com.tastyhouse.core.domain.coupon.domain.vo.MemberCouponId;
import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.coupon.application.dto.result.MemberCouponItemResult;
import com.tastyhouse.core.domain.coupon.application.dto.result.MemberCouponResult;
import com.tastyhouse.core.shared.page.PageQuery;
import com.tastyhouse.core.shared.page.PageResult;

public interface MemberCouponRepository {

    Optional<MemberCoupon> findById(MemberCouponId id);

    List<MemberCouponResult> findWithCouponByMemberId(MemberId memberId);

    List<MemberCouponResult> findAvailableWithCouponByMemberId(MemberId memberId, LocalDateTime now);

    PageResult<MemberCouponItemResult> findByCouponId(CouponId couponId, PageQuery pageQuery);

    boolean existsByMemberIdAndCouponId(MemberId memberId, CouponId couponId);

    MemberCoupon save(MemberCoupon memberCoupon);
}
