package com.tastyhouse.application.coupon.service;

import com.tastyhouse.application.shared.marker.AdminApp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.coupon.model.DiscountType;
import com.tastyhouse.domain.coupon.vo.CouponId;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.application.coupon.port.out.CouponDetailResult;
import com.tastyhouse.application.coupon.port.out.CouponListItemResult;
import com.tastyhouse.application.coupon.port.out.CouponManagementQueryPort;
import com.tastyhouse.application.coupon.port.out.CouponSearchCondition;
import com.tastyhouse.application.coupon.port.out.MemberCouponItemResult;
import com.tastyhouse.application.coupon.port.in.CouponManagementQueryUseCase;

@Service
@AdminApp
@Transactional(readOnly = true)
public class CouponManagementQueryService implements CouponManagementQueryUseCase {

    private final CouponManagementQueryPort couponManagementQueryPort;

    public CouponManagementQueryService(CouponManagementQueryPort couponManagementQueryPort) {
        this.couponManagementQueryPort = couponManagementQueryPort;
    }

    @Override
    public PageResult<CouponListItemResult> getCoupons(
        String name,
        String discountType,
        Boolean visible,
        int page,
        int size
    ) {
        DiscountType type = discountType == null ? null : DiscountType.from(discountType);
        CouponSearchCondition condition = CouponSearchCondition.of(name, type, visible);
        PageQuery pageQuery = PageQuery.of(page, size);

        return couponManagementQueryPort.findAllCoupons(condition, pageQuery);
    }

    @Override
    public CouponDetailResult getCoupon(Long id) {
        return couponManagementQueryPort.findCouponDetailById(CouponId.of(id))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.COUPON_NOT_FOUND));
    }

    @Override
    public PageResult<MemberCouponItemResult> getIssuedCoupons(Long id, int page, int size) {
        PageQuery pageQuery = PageQuery.of(page, size);

        return couponManagementQueryPort.findIssuedMemberCoupons(CouponId.of(id), pageQuery);
    }
}
