package com.tastyhouse.application.coupon.port.out;

import java.util.Optional;

import com.tastyhouse.domain.coupon.vo.CouponId;
import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;

public interface CouponManagementQueryPort {

    PageResult<CouponListItemResult> findAllCoupons(CouponSearchCondition condition, PageQuery pageQuery);

    Optional<CouponDetailResult> findCouponDetailById(CouponId couponId);

    PageResult<MemberCouponItemResult> findIssuedMemberCoupons(CouponId couponId, PageQuery pageQuery);
}
