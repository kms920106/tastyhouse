package com.tastyhouse.application.coupon.port.out.write;

import java.util.Optional;

import com.tastyhouse.domain.coupon.model.Coupon;
import com.tastyhouse.domain.coupon.vo.CouponId;

public interface CouponPersistencePort {
    Optional<Coupon> findById(CouponId id);

    Coupon save(Coupon coupon);
}
