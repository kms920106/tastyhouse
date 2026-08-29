package com.tastyhouse.adminapi.coupon.application.port.in;

import com.tastyhouse.adminapi.coupon.adapter.in.web.response.CouponDetailResponse;
import com.tastyhouse.adminapi.coupon.adapter.in.web.response.CouponListItemResponse;
import com.tastyhouse.adminapi.coupon.adapter.in.web.response.MemberCouponItemResponse;
import com.tastyhouse.apicommon.common.PaginationResponse;

/**
 * 쿠폰 조회 인바운드 포트.
 *
 * <p>컨트롤러는 이 인터페이스만 주입하고 구현({@code CouponQueryService})을 알지 않는다. 도입 근거는
 * 다형성이 아니라 컴파일 게이트와 경계 계약의 문서화다(backend/CLAUDE.md 인바운드 포트 절).
 */
public interface CouponQueryUseCase {

    PaginationResponse<CouponListItemResponse> getCoupons(
        String name,
        String discountType,
        Boolean visible,
        int page,
        int size
    );

    CouponDetailResponse getCoupon(Long id);

    PaginationResponse<MemberCouponItemResponse> getIssuedCoupons(Long id, int page, int size);
}
