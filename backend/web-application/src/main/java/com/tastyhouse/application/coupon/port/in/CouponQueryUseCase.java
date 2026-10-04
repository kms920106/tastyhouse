package com.tastyhouse.application.coupon.port.in;

import java.util.List;

import com.tastyhouse.application.coupon.port.out.MyCouponListItemResult;

public interface CouponQueryUseCase {

    List<MyCouponListItemResult> getMyCoupons(Long memberId);

    List<MyCouponListItemResult> getMyAvailableCoupons(Long memberId);
}
