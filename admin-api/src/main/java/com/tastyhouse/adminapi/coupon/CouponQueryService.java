package com.tastyhouse.adminapi.coupon;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.coupon.domain.model.DiscountType;
import com.tastyhouse.domain.coupon.domain.vo.CouponId;
import com.tastyhouse.domain.exception.EntityNotFoundException;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.shared.page.PageQuery;
import com.tastyhouse.domain.shared.page.PageResult;
import com.tastyhouse.infrastructure.coupon.query.CouponDetailResult;
import com.tastyhouse.infrastructure.coupon.query.CouponListItemResult;
import com.tastyhouse.infrastructure.coupon.query.CouponQueryDao;
import com.tastyhouse.infrastructure.coupon.query.CouponSearchCondition;
import com.tastyhouse.infrastructure.coupon.query.MemberCouponItemResult;
import com.tastyhouse.adminapi.common.PaginationResponse;
import com.tastyhouse.adminapi.coupon.response.CouponDetailResponse;
import com.tastyhouse.adminapi.coupon.response.CouponListItemResponse;
import com.tastyhouse.adminapi.coupon.response.MemberCouponItemResponse;

/**
 * 쿠폰 관리 조회 서비스(admin).
 *
 * <p>infra read 어댑터({@link CouponQueryDao})만 주입해 조회하고 Response를 조립한다(패턴 2/3). 도메인
 * write 포트를 주입하지 않으므로 조회 경로가 도메인 모델을 거치지 않는다.
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CouponQueryService {

    private final CouponQueryDao couponQueryDao;

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

        PageResult<CouponListItemResponse> pageResult = couponQueryDao.findAllCoupons(condition, pageQuery)
            .map(this::toCouponListItemResponse);
        return PaginationResponse.from(pageResult);
    }

    public CouponDetailResponse getCoupon(Long id) {
        CouponDetailResult couponDetail = couponQueryDao.findCouponDetailById(CouponId.of(id))
            .orElseThrow(() -> new EntityNotFoundException(ErrorCode.COUPON_NOT_FOUND));
        return toCouponDetailResponse(couponDetail);
    }

    public PaginationResponse<MemberCouponItemResponse> getIssuedCoupons(Long id, int page, int size) {
        PageQuery pageQuery = PageQuery.of(page, size);

        PageResult<MemberCouponItemResponse> pageResult =
            couponQueryDao.findIssuedMemberCoupons(CouponId.of(id), pageQuery)
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
            dto.memberId().value(),
            dto.used(),
            dto.usedAt(),
            dto.expiredAt(),
            dto.createdAt()
        );
    }
}
