package com.tastyhouse.application.coupon.port.in;

import com.tastyhouse.application.shared.marker.AdminApp;

@AdminApp
public interface CouponCommandUseCase {

    Long createCoupon(CouponCreateCommand command);

    void updateCoupon(CouponUpdateCommand command);

    void deleteCoupon(CouponDeleteCommand command);

    Long issueCoupon(CouponIssueCommand command);
}
