package com.tastyhouse.adminapi.coupon;

import java.time.LocalDateTime;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.tastyhouse.core.domain.coupon.domain.model.DiscountType;
import com.tastyhouse.core.domain.coupon.domain.vo.CouponId;
import com.tastyhouse.core.domain.member.domain.vo.MemberId;
import com.tastyhouse.core.domain.coupon.application.CouponCommandService;
import com.tastyhouse.core.domain.coupon.application.CouponQueryService;
import com.tastyhouse.core.domain.coupon.application.dto.CouponDetailDto;
import com.tastyhouse.core.domain.coupon.application.dto.CouponSearchCondition;
import com.tastyhouse.core.domain.coupon.application.dto.command.CouponCreateCommand;
import com.tastyhouse.core.domain.coupon.application.dto.command.CouponUpdateCommand;
import com.tastyhouse.core.shared.page.PageResult;
import com.tastyhouse.adminapi.coupon.response.CouponDetailResponse;
import com.tastyhouse.adminapi.coupon.response.CouponListItemResponse;
import com.tastyhouse.adminapi.coupon.response.CouponPageResponse;
import com.tastyhouse.adminapi.coupon.response.MemberCouponAdminItemResponse;
import com.tastyhouse.adminapi.coupon.response.MemberCouponPageResponse;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponCommandService couponCommandService;
    private final CouponQueryService couponQueryService;

    public CouponPageResponse getCoupons(String name, String discountType, Boolean visible, int page, int size) {
        DiscountType type = discountType == null ? null : DiscountType.from(discountType);
        CouponSearchCondition condition = CouponSearchCondition.of(name, type, visible);
        PageResult<CouponListItemResponse> pageResult = couponQueryService.findAllCoupons(condition, page, size)
            .map(CouponListItemResponse::from);
        return CouponPageResponse.from(pageResult);
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
        CouponDetailDto couponDetail = couponQueryService.findDetailById(couponId);
        return CouponDetailResponse.from(couponDetail);
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

    public MemberCouponPageResponse getIssuedCoupons(Long couponId, int page, int size) {
        PageResult<MemberCouponAdminItemResponse> pageResult = couponQueryService.findIssuedByCoupon(CouponId.of(couponId), page, size)
            .map(MemberCouponAdminItemResponse::from);
        return MemberCouponPageResponse.from(pageResult);
    }
}
