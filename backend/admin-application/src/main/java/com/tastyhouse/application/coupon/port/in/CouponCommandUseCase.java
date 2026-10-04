package com.tastyhouse.application.coupon.port.in;

public interface CouponCommandUseCase {

    Long createCoupon(CouponCreateCommand command);

    void updateCoupon(CouponUpdateCommand command);

    void deleteCoupon(CouponDeleteCommand command);

    Long issueCoupon(CouponIssueCommand command);
}
