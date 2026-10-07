package com.tastyhouse.application.coupon.port.in;

import java.util.List;

import com.tastyhouse.application.coupon.port.out.MyCouponListItemResult;

public interface CouponMyListQueryUseCase {

    List<MyCouponListItemResult> getMyCoupons(Long memberId);
}
