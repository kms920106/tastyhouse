package com.tastyhouse.application.coupon.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.coupon.model.Coupon;
import com.tastyhouse.domain.coupon.model.DiscountType;
import com.tastyhouse.application.coupon.port.in.CouponCreateCommand;
import com.tastyhouse.application.coupon.port.in.CouponCreateUseCase;
import com.tastyhouse.application.coupon.port.out.write.CouponSavePort;

@Service
@Transactional
class CouponCreateService implements CouponCreateUseCase {

    private final CouponSavePort couponSavePort;

    public CouponCreateService(CouponSavePort couponSavePort) {
        this.couponSavePort = couponSavePort;
    }

    @Override
    public Long createCoupon(CouponCreateCommand command) {
        Coupon coupon = Coupon.of(
            command.name(),
            command.description(),
            DiscountType.from(command.discountType()),
            command.discountAmount(),
            command.maxDiscountAmount(),
            command.minOrderAmount(),
            command.maxDiscountCount(),
            command.issueStartAt(),
            command.issueEndAt(),
            command.useStartAt(),
            command.useEndAt(),
            command.visible()
        );
        Coupon saved = couponSavePort.save(coupon);
        return saved.getCouponId().value();
    }
}
