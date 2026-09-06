package com.tastyhouse.application.coupon.port.in;

import com.tastyhouse.application.shared.marker.WebApp;
import java.util.List;

import com.tastyhouse.application.coupon.port.out.MyCouponListItemResult;

@WebApp
public interface CouponQueryUseCase {

    List<MyCouponListItemResult> getMyCoupons(Long memberId);

    List<MyCouponListItemResult> getMyAvailableCoupons(Long memberId);
}
