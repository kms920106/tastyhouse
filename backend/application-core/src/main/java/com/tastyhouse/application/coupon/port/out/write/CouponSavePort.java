package com.tastyhouse.application.coupon.port.out.write;

import com.tastyhouse.domain.coupon.model.Coupon;

public interface CouponSavePort {

    Coupon save(Coupon coupon);
}
