package com.tastyhouse.application.coupon.port.in;

import com.tastyhouse.application.coupon.port.out.MemberCouponItemResult;
import com.tastyhouse.application.shared.port.out.page.PageResult;

public interface CouponManagementIssuedListQueryUseCase {

    PageResult<MemberCouponItemResult> getIssuedCoupons(Long id, int page, int size);
}
