package com.tastyhouse.domain.coupon.repository;

import java.util.Optional;

import com.tastyhouse.domain.coupon.model.Coupon;
import com.tastyhouse.domain.coupon.vo.CouponId;

public interface CouponRepository {
    Optional<Coupon> findById(CouponId id);

    Coupon save(Coupon coupon);
}
