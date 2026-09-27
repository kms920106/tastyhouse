package com.tastyhouse.application.coupon.port.out.write;

import java.util.Optional;

public interface CouponStatePort {
    Optional<CouponState> findById(Long id);

    CouponState save(CouponState state);
}
