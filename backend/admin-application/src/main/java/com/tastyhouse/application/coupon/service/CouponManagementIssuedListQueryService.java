package com.tastyhouse.application.coupon.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.coupon.vo.CouponId;
import com.tastyhouse.application.coupon.port.in.CouponManagementIssuedListQueryUseCase;
import com.tastyhouse.application.coupon.port.out.CouponManagementQueryPort;
import com.tastyhouse.application.coupon.port.out.MemberCouponItemResult;
import com.tastyhouse.application.shared.port.out.page.PageQuery;
import com.tastyhouse.application.shared.port.out.page.PageResult;

@Service
@Transactional(readOnly = true)
class CouponManagementIssuedListQueryService implements CouponManagementIssuedListQueryUseCase {

    private final CouponManagementQueryPort couponManagementQueryPort;

    public CouponManagementIssuedListQueryService(CouponManagementQueryPort couponManagementQueryPort) {
        this.couponManagementQueryPort = couponManagementQueryPort;
    }

    @Override
    public PageResult<MemberCouponItemResult> getIssuedCoupons(Long id, int page, int size) {
        PageQuery pageQuery = PageQuery.of(page, size);

        return couponManagementQueryPort.findIssuedMemberCoupons(CouponId.of(id).value(), pageQuery);
    }
}
