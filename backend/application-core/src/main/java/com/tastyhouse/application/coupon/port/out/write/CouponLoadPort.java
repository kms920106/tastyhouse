package com.tastyhouse.application.coupon.port.out.write;

import java.util.Optional;

import com.tastyhouse.domain.coupon.model.Coupon;
import com.tastyhouse.domain.coupon.vo.CouponId;

public interface CouponLoadPort {

    Optional<Coupon> findActiveById(CouponId id);
}
