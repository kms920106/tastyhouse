package com.tastyhouse.core.domain.coupon.domain.repository;

import com.tastyhouse.core.domain.coupon.domain.model.Coupon;
import com.tastyhouse.core.domain.coupon.domain.model.CouponId;

import java.util.Optional;

public interface CouponRepository {

    Optional<Coupon> findById(CouponId id);

    Coupon save(Coupon coupon);
}
