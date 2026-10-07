package com.tastyhouse.application.coupon.port.in;

import com.tastyhouse.application.coupon.port.out.CouponDetailResult;

public interface CouponManagementDetailQueryUseCase {

    CouponDetailResult getCoupon(Long id);
}
