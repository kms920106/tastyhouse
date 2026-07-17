package com.tastyhouse.adminapi.coupon;

import java.time.LocalDateTime;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.tastyhouse.core.domain.coupon.domain.model.DiscountType;
import com.tastyhouse.core.domain.coupon.domain.vo.CouponId;
import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.coupon.application.CouponCommandService;
import com.tastyhouse.core.domain.coupon.application.CouponQueryService;
import com.tastyhouse.core.domain.coupon.application.dto.CouponSearchCondition;
import com.tastyhouse.core.domain.coupon.application.dto.command.CouponCreateCommand;
import com.tastyhouse.core.domain.coupon.application.dto.command.CouponUpdateCommand;
import com.tastyhouse.core.domain.coupon.application.dto.result.CouponDetailResult;
import com.tastyhouse.core.domain.coupon.application.dto.result.CouponListItemResult;
import com.tastyhouse.core.domain.coupon.application.dto.result.MemberCouponItemResult;
import com.tastyhouse.core.shared.page.PageResult;
import com.tastyhouse.adminapi.common.PaginationResponse;
import com.tastyhouse.adminapi.coupon.response.CouponDetailResponse;
import com.tastyhouse.adminapi.coupon.response.CouponListItemResponse;
import com.tastyhouse.adminapi.coupon.response.MemberCouponAdminItemResponse;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponCommandService couponCommandService;
    private final CouponQueryService couponQueryService;

    public PaginationResponse<CouponListItemResponse> getCoupons(String name, String discountType, Boolean visible, int page, int size) {
        DiscountType type = discountType == null ? null : DiscountType.from(discountType);
        CouponSearchCondition condition = CouponSearchCondition.of(name, type, visible);
        PageResult<CouponListItemResponse> pageResult = couponQueryService.findAllCoupons(condition, page, size)
            .map(this::toCouponListItemResponse);
        return PaginationResponse.from(pageResult);
    }

    public Long createCoupon(
        String name,
        String description,
        String discountType,
        Integer discountAmount,
        Integer maxDiscountAmount,
        Integer minOrderAmount,
        Integer maxDiscountCount,
        LocalDateTime issueStartAt,
        LocalDateTime issueEndAt,
        LocalDateTime useStartAt,
        LocalDateTime useEndAt,
        boolean visible
    ) {
        CouponCreateCommand command = CouponCreateCommand.of(
            name, description, DiscountType.from(discountType), discountAmount, maxDiscountAmount,
            minOrderAmount, maxDiscountCount, issueStartAt, issueEndAt, useStartAt, useEndAt, visible
        );
        CouponId couponId = couponCommandService.createCoupon(command);
        return couponId.value();
    }

    public CouponDetailResponse getCoupon(Long id) {
        CouponId couponId = CouponId.of(id);
        CouponDetailResult couponDetail = couponQueryService.findDetailById(couponId);
        return toCouponDetailResponse(couponDetail);
    }

    public void updateCoupon(
        Long id,
        String name,
        String description,
        String discountType,
        Integer discountAmount,
        Integer maxDiscountAmount,
        Integer minOrderAmount,
        Integer maxDiscountCount,
        LocalDateTime issueStartAt,
        LocalDateTime issueEndAt,
        LocalDateTime useStartAt,
        LocalDateTime useEndAt,
        boolean visible
    ) {
        CouponId couponId = CouponId.of(id);
        CouponUpdateCommand command = CouponUpdateCommand.of(
            name, description, DiscountType.from(discountType), discountAmount, maxDiscountAmount,
            minOrderAmount, maxDiscountCount, issueStartAt, issueEndAt, useStartAt, useEndAt, visible
        );
        couponCommandService.updateCoupon(couponId, command);
    }

    public void deleteCoupon(Long id) {
        CouponId couponId = CouponId.of(id);
        couponCommandService.deleteCoupon(couponId);
    }

    public Long issueCoupon(Long couponId, Long memberId) {
        CouponId targetCouponId = CouponId.of(couponId);
        MemberId targetMemberId = MemberId.of(memberId);
        return couponCommandService.issueCouponByAdmin(targetCouponId, targetMemberId).value();
    }

    public PaginationResponse<MemberCouponAdminItemResponse> getIssuedCoupons(Long couponId, int page, int size) {
        PageResult<MemberCouponAdminItemResponse> pageResult = couponQueryService.findIssuedByCoupon(CouponId.of(couponId), page, size)
            .map(this::toMemberCouponAdminItemResponse);
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
            dto.couponId().value(),
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

    private MemberCouponAdminItemResponse toMemberCouponAdminItemResponse(MemberCouponItemResult dto) {
        return MemberCouponAdminItemResponse.from(
            dto.id(),
            dto.memberId().value(),
            dto.used(),
            dto.usedAt(),
            dto.expiredAt(),
            dto.createdAt()
        );
    }
}
