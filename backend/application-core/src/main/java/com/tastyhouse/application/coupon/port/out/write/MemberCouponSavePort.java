package com.tastyhouse.application.coupon.port.out.write;

import com.tastyhouse.domain.coupon.model.MemberCoupon;

public interface MemberCouponSavePort {

    MemberCoupon save(MemberCoupon memberCoupon);
}
