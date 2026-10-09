package com.tastyhouse.application.coupon.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.coupon.model.Coupon;
import com.tastyhouse.domain.coupon.model.DiscountType;
import com.tastyhouse.domain.coupon.vo.CouponId;
import com.tastyhouse.application.coupon.port.in.CouponUpdateCommand;
import com.tastyhouse.application.coupon.port.in.CouponUpdateUseCase;
import com.tastyhouse.application.coupon.port.out.write.CouponLoadPort;
import com.tastyhouse.application.coupon.port.out.write.CouponSavePort;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
@Transactional
class CouponUpdateService implements CouponUpdateUseCase {

    private final CouponLoadPort couponLoadPort;
    private final CouponSavePort couponSavePort;

    public CouponUpdateService(CouponLoadPort couponLoadPort, CouponSavePort couponSavePort) {
        this.couponLoadPort = couponLoadPort;
        this.couponSavePort = couponSavePort;
    }

    @Override
    public void updateCoupon(CouponUpdateCommand command) {
        CouponId couponId = CouponId.of(command.couponId());
        Coupon coupon = findCouponOrThrow(couponId);

        coupon.update(
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
        couponSavePort.save(coupon);
    }

    private Coupon findCouponOrThrow(CouponId couponId) {
        return couponLoadPort.findActiveById(couponId)
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.COUPON_NOT_FOUND));
    }
}
