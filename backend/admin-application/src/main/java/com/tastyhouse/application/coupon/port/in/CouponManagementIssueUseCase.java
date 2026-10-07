package com.tastyhouse.application.coupon.port.in;

public interface CouponManagementIssueUseCase {

    Long issueCoupon(CouponIssueCommand command);
}
