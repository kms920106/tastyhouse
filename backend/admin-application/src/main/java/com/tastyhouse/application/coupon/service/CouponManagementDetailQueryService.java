package com.tastyhouse.application.coupon.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.coupon.vo.CouponId;
import com.tastyhouse.application.coupon.port.in.CouponManagementDetailQueryUseCase;
import com.tastyhouse.application.coupon.port.out.CouponDetailResult;
import com.tastyhouse.application.coupon.port.out.CouponManagementQueryPort;
import com.tastyhouse.application.shared.exception.ApplicationErrorCode;
import com.tastyhouse.application.shared.exception.ResourceNotFoundException;

@Service
@Transactional(readOnly = true)
class CouponManagementDetailQueryService implements CouponManagementDetailQueryUseCase {

    private final CouponManagementQueryPort couponManagementQueryPort;

    public CouponManagementDetailQueryService(CouponManagementQueryPort couponManagementQueryPort) {
        this.couponManagementQueryPort = couponManagementQueryPort;
    }

    @Override
    public CouponDetailResult getCoupon(Long id) {
        return couponManagementQueryPort.findCouponDetailById(CouponId.of(id).value())
            .orElseThrow(() -> new ResourceNotFoundException(ApplicationErrorCode.COUPON_NOT_FOUND));
    }
}
