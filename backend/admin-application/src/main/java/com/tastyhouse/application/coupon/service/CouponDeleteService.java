package com.tastyhouse.application.coupon.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.coupon.model.Coupon;
import com.tastyhouse.domain.coupon.vo.CouponId;
import com.tastyhouse.application.coupon.port.in.CouponDeleteCommand;
import com.tastyhouse.application.coupon.port.in.CouponDeleteUseCase;
import com.tastyhouse.application.coupon.port.out.write.CouponLoadPort;
import com.tastyhouse.application.coupon.port.out.write.CouponSavePort;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
@Transactional
class CouponDeleteService implements CouponDeleteUseCase {

    private final CouponLoadPort couponLoadPort;
    private final CouponSavePort couponSavePort;

    public CouponDeleteService(CouponLoadPort couponLoadPort, CouponSavePort couponSavePort) {
        this.couponLoadPort = couponLoadPort;
        this.couponSavePort = couponSavePort;
    }

    @Override
    public void deleteCoupon(CouponDeleteCommand command) {
        CouponId couponId = CouponId.of(command.couponId());
        Coupon coupon = findCouponOrThrow(couponId);

        coupon.delete();
        couponSavePort.save(coupon);
    }

    private Coupon findCouponOrThrow(CouponId couponId) {
        return couponLoadPort.findActiveById(couponId)
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.COUPON_NOT_FOUND));
    }
}
