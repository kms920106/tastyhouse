package com.tastyhouse.application.coupon.port.in;

public interface CouponCreateUseCase {

    Long createCoupon(CouponCreateCommand command);
}
