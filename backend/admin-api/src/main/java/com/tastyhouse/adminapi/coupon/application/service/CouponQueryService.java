package com.tastyhouse.adminapi.coupon.application.service;

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
import com.tastyhouse.application.coupon.port.out.CouponQueryPort;
import com.tastyhouse.application.coupon.port.out.CouponSearchCondition;
import com.tastyhouse.application.coupon.port.out.MemberCouponItemResult;
import com.tastyhouse.apicommon.common.PaginationResponse;
import com.tastyhouse.adminapi.coupon.adapter.in.web.response.CouponDetailResponse;
import com.tastyhouse.adminapi.coupon.adapter.in.web.response.CouponListItemResponse;
import com.tastyhouse.adminapi.coupon.adapter.in.web.response.MemberCouponItemResponse;
import com.tastyhouse.adminapi.coupon.application.port.in.CouponQueryUseCase;

/**
 * 쿠폰 관리 조회 서비스(admin).
 *
 * <p>읽기 포트({@link CouponQueryPort})만 주입해 조회하고 Response를 조립한다(패턴 2/3). 도메인
 * write 포트를 주입하지 않으므로 조회 경로가 도메인 모델을 거치지 않는다.
 */
@Service
@Transactional(readOnly = true)
public class CouponQueryService implements CouponQueryUseCase {

    private final CouponQueryPort couponQueryPort;

    public CouponQueryService(CouponQueryPort couponQueryPort) {
        this.couponQueryPort = couponQueryPort;
    }

    @Override
    public PaginationResponse<CouponListItemResponse> getCoupons(
        String name,
        String discountType,
        Boolean visible,
        int page,
        int size
    ) {
        DiscountType type = discountType == null ? null : DiscountType.from(discountType);
        CouponSearchCondition condition = CouponSearchCondition.of(name, type, visible);
        PageQuery pageQuery = PageQuery.of(page, size);

        PageResult<CouponListItemResponse> pageResult = couponQueryPort.findAllCoupons(condition, pageQuery)
            .map(this::toCouponListItemResponse);
        return PaginationResponse.from(pageResult);
    }

    @Override
    public CouponDetailResponse getCoupon(Long id) {
        CouponDetailResult couponDetail = couponQueryPort.findCouponDetailById(CouponId.of(id))
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.COUPON_NOT_FOUND));
        return toCouponDetailResponse(couponDetail);
    }

    @Override
    public PaginationResponse<MemberCouponItemResponse> getIssuedCoupons(Long id, int page, int size) {
        PageQuery pageQuery = PageQuery.of(page, size);

        PageResult<MemberCouponItemResponse> pageResult =
            couponQueryPort.findIssuedMemberCoupons(CouponId.of(id), pageQuery)
                .map(this::toMemberCouponItemResponse);
        return PaginationResponse.from(pageResult);
    }

    private CouponListItemResponse toCouponListItemResponse(CouponListItemResult dto) {
        return CouponListItemResponse.from(
            dto.id(),
            dto.name(),
            dto.discountType().name(),
            dto.discountAmount(),
            dto.maxDiscountAmount(),
            dto.minOrderAmount(),
            dto.maxDiscountCount(),
            dto.issueStartAt(),
            dto.issueEndAt(),
            dto.useStartAt(),
            dto.useEndAt(),
            dto.visible()
        );
    }

    private CouponDetailResponse toCouponDetailResponse(CouponDetailResult dto) {
        return CouponDetailResponse.from(
            dto.id(),
            dto.name(),
            dto.description(),
            dto.discountType().name(),
            dto.discountAmount(),
            dto.maxDiscountAmount(),
            dto.minOrderAmount(),
            dto.maxDiscountCount(),
            dto.issueStartAt(),
            dto.issueEndAt(),
            dto.useStartAt(),
            dto.useEndAt(),
            dto.visible(),
            dto.createdAt(),
            dto.updatedAt()
        );
    }

    private MemberCouponItemResponse toMemberCouponItemResponse(MemberCouponItemResult dto) {
        return MemberCouponItemResponse.from(
            dto.id(),
            dto.memberId(),
            dto.used(),
            dto.usedAt(),
            dto.expiredAt(),
            dto.createdAt()
        );
    }
}
