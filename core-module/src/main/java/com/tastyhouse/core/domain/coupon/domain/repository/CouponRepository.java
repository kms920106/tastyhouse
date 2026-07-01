package com.tastyhouse.core.domain.coupon.domain.repository;

import java.util.Optional;

import com.tastyhouse.core.domain.coupon.domain.model.Coupon;
import com.tastyhouse.core.domain.coupon.domain.model.CouponId;

public interface CouponRepository {

    Optional<Coupon> findById(CouponId id);

    Coupon save(Coupon coupon);
}
