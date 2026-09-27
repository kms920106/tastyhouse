package com.tastyhouse.application.coupon.store;

import java.util.Optional;

import com.tastyhouse.application.coupon.port.out.write.CouponStatePort;
import com.tastyhouse.domain.coupon.model.Coupon;
import com.tastyhouse.domain.coupon.vo.CouponId;

public class CouponStore implements CouponRepository {
    private final CouponStatePort couponStatePort;

    public CouponStore(CouponStatePort couponStatePort) {
        this.couponStatePort = couponStatePort;
    }

    @Override
    public Optional<Coupon> findById(CouponId id) {
        if (id == null) {
            return Optional.empty();
        }
        return couponStatePort.findById(id.value()).map(CouponStateMapper::toDomain);
    }

    @Override
    public Coupon save(Coupon coupon) {
        return CouponStateMapper.toDomain(couponStatePort.save(CouponStateMapper.toState(coupon)));
    }
}
