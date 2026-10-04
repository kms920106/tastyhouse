package com.tastyhouse.application.coupon.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tastyhouse.domain.coupon.model.Coupon;
import com.tastyhouse.domain.coupon.model.DiscountType;
import com.tastyhouse.domain.coupon.vo.CouponId;
import com.tastyhouse.domain.exception.ErrorCode;
import com.tastyhouse.domain.exception.ResourceNotFoundException;
import com.tastyhouse.domain.member.vo.MemberId;
import com.tastyhouse.application.coupon.port.in.CouponCommandUseCase;
import com.tastyhouse.application.coupon.port.in.CouponCreateCommand;
import com.tastyhouse.application.coupon.port.in.CouponDeleteCommand;
import com.tastyhouse.application.coupon.port.in.CouponIssueCommand;
import com.tastyhouse.application.coupon.port.in.CouponUpdateCommand;
import com.tastyhouse.application.coupon.port.out.write.CouponPersistencePort;

@Service
@Transactional
class CouponCommandService implements CouponCommandUseCase {

    private final CouponPersistencePort couponPersistencePort;
    private final CouponIssueService couponIssueService;

    public CouponCommandService(CouponPersistencePort couponPersistencePort, CouponIssueService couponIssueService) {
        this.couponPersistencePort = couponPersistencePort;
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
        Coupon saved = couponPersistencePort.save(coupon);
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
        couponPersistencePort.save(coupon);
    }

    @Override
    public void deleteCoupon(CouponDeleteCommand command) {
        CouponId couponId = CouponId.of(command.couponId());
        Coupon coupon = findCouponOrThrow(couponId);

        coupon.delete();
        couponPersistencePort.save(coupon);
    }

    @Override
    public Long issueCoupon(CouponIssueCommand command) {
        CouponId couponId = CouponId.of(command.couponId());
        MemberId targetMemberId = MemberId.of(command.memberId());
        return couponIssueService.issueCoupon(targetMemberId, couponId).value();
    }

    private Coupon findCouponOrThrow(CouponId couponId) {
        return couponPersistencePort.findById(couponId)
            .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.COUPON_NOT_FOUND));
    }
}
