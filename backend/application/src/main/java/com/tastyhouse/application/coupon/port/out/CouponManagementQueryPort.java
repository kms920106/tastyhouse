package com.tastyhouse.application.coupon.port.out;

import java.util.Optional;

import com.tastyhouse.domain.coupon.vo.CouponId;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;

public interface CouponManagementQueryPort {

    PageResult<CouponListItemResult> findAllCoupons(CouponSearchCondition condition, PageQuery pageQuery);

    Optional<CouponDetailResult> findCouponDetailById(CouponId couponId);

    PageResult<MemberCouponItemResult> findIssuedMemberCoupons(CouponId couponId, PageQuery pageQuery);
}
