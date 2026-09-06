package com.tastyhouse.application.coupon.port.in;

import com.tastyhouse.application.coupon.port.out.CouponDetailResult;
import com.tastyhouse.application.coupon.port.out.CouponListItemResult;
import com.tastyhouse.application.coupon.port.out.MemberCouponItemResult;
import com.tastyhouse.application.shared.marker.AdminApp;
import com.tastyhouse.domain.shared.page.PageResult;

@AdminApp
public interface CouponManagementQueryUseCase {

    PageResult<CouponListItemResult> getCoupons(
        String name,
        String discountType,
        Boolean visible,
        int page,
        int size
    );

    CouponDetailResult getCoupon(Long id);

    PageResult<MemberCouponItemResult> getIssuedCoupons(Long id, int page, int size);
}
