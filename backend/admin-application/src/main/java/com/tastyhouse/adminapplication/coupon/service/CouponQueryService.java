package com.tastyhouse.adminapplication.coupon.service;

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
import com.tastyhouse.adminapplication.coupon.port.in.CouponQueryUseCase;

/**
 * 쿠폰 관리 조회 서비스(admin).
 *
 * <p>읽기 포트({@link CouponManagementQueryPort})만 주입해 조회한다(패턴 2/3). 도메인 write 포트를
 * 주입하지 않으므로 조회 경로가 도메인 모델을 거치지 않는다.
 *
 * <p><b>챕터 06</b> — 읽기 포트의 {@code *Result}를 그대로 반환하고 Response로 변환하지 않는다.
 * 표현 계약(@Schema 붙은 Response·PaginationResponse) 조립은 컨트롤러의 책임이다.
 */
@Service
@Transactional(readOnly = true)
public class CouponQueryService implements CouponQueryUseCase {

    private final CouponManagementQueryPort couponManagementQueryPort;

    public CouponQueryService(CouponManagementQueryPort couponManagementQueryPort) {
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
