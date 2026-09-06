package com.tastyhouse.application.coupon.service;

import com.tastyhouse.application.shared.marker.AdminApp;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.application.coupon.port.in.CouponCommandUseCase;
import com.tastyhouse.application.coupon.port.in.CouponCreateCommand;
import com.tastyhouse.application.coupon.port.in.CouponDeleteCommand;
import com.tastyhouse.application.coupon.port.in.CouponIssueCommand;
import com.tastyhouse.application.coupon.port.in.CouponUpdateCommand;
import com.tastyhouse.domain.coupon.model.Coupon;
import com.tastyhouse.domain.coupon.model.DiscountType;
import com.tastyhouse.domain.coupon.repository.CouponRepository;
import com.tastyhouse.domain.coupon.service.CouponIssueService;
import com.tastyhouse.domain.coupon.vo.CouponId;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;

@Service
@AdminApp
@Transactional
public class CouponCommandService implements CouponCommandUseCase {

    private final CouponRepository couponRepository;
    private final CouponIssueService couponIssueService;

    public CouponCommandService(CouponRepository couponRepository, CouponIssueService couponIssueService) {
        this.couponRepository = couponRepository;
        this.couponIssueService = couponIssueService;
    }

    @Override
    public Long createCoupon(CouponCreateCommand command) {
        Coupon coupon = Coupon.of(
            command.name(),
            command.description(),
            DiscountType.from(command.discountType()),
            command.discountAmount(),
            command.maxDiscountAmount(),
            command.minOrderAmount(),
            command.maxDiscountCount(),
            command.issueStartAt(),
            command.issueEndAt(),
            command.useStartAt(),
            command.useEndAt(),
            command.visible()
        );
        Coupon saved = couponRepository.save(coupon);
        return saved.getCouponId().value();
    }

    @Override
    public void updateCoupon(CouponUpdateCommand command) {
        CouponId couponId = CouponId.of(command.couponId());
        Coupon coupon = findCouponOrThrow(couponId);

        coupon.update(
            command.name(),
            command.description(),
            DiscountType.from(command.discountType()),
            command.discountAmount(),
            command.maxDiscountAmount(),
            command.minOrderAmount(),
            command.maxDiscountCount(),
            command.issueStartAt(),
            command.issueEndAt(),
            command.useStartAt(),
            command.useEndAt(),
            command.visible()
        );
        couponRepository.save(coupon);
    }

    @Override
    public void deleteCoupon(CouponDeleteCommand command) {
        CouponId couponId = CouponId.of(command.couponId());
        Coupon coupon = findCouponOrThrow(couponId);

        coupon.delete();
        couponRepository.save(coupon);
    }

    @Override
    public Long issueCoupon(CouponIssueCommand command) {
        CouponId couponId = CouponId.of(command.couponId());
        MemberId targetMemberId = MemberId.of(command.memberId());
        return couponIssueService.issueCoupon(targetMemberId, couponId).value();
    }

    private Coupon findCouponOrThrow(CouponId couponId) {
        return couponRepository.findById(couponId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.COUPON_NOT_FOUND));
    }
}
